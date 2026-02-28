package com.hl.hlaiagent.chatmemory;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件系统的聊天记忆实现
 */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    static {
        // 允许不预先注册类，适用于动态类型
        kryo.setRegistrationRequired(false);
        // 使用Objenesis策略，允许实例化没有默认构造函数的类
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(BASE_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        // 直接调用批量添加方法，避免覆盖历史消息
        add(conversationId, List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取当前会话的历史消息
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        // 合并新消息和历史消息
        List<Message> mutableConversation = new ArrayList<>(conversationMessages);
        mutableConversation.addAll(messages);
        // 保存合并后的消息列表
        saveConversation(conversationId, mutableConversation);
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    public List<Message> get(String conversationId, int lastN) {
        List<Message> conversation = getOrCreateConversation(conversationId);
        // 返回最后N条消息，如果总消息数少于N，则返回全部消息
        return conversation.stream()
                .skip(Math.max(0, conversation.size() - lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File conversationFile = getConversationFile(conversationId);
        if (conversationFile.exists()) {
            conversationFile.delete();
        }
    }


    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取会话对应的文件对象
     *
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }

    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

}
