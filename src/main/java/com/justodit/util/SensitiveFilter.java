package com.justodit.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//敏感词过滤器
@Component
public class SensitiveFilter {


    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct //服务启动时调用
    public void init(){

        try (
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));//字符流
        ) {
           String keyWord;
           while ((keyWord = reader.readLine()) != null){

               //添加到前缀数中
               this.addKeyWord(keyWord);
           }

        } catch (Exception e) {
            logger.error("加载敏感词文件失败"+e.getMessage());
        }

    }

    /**
     * 将一个敏感词添加到前缀数中
     * @param keyWord
     */
    private void addKeyWord(String keyWord){
        TrieNode tempNode = rootNode; //指针,默认指向根
        for (int i = 0; i < keyWord.length() ;i++){
            char c = keyWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //如果子节点不存在,就创建,将字符挂载到节点上
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指针指向子节点,进入下一轮循环
            tempNode = subNode;

            //设置结束的标识
            if (i == keyWord.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        //指针1 指向树
        TrieNode tempNode = rootNode;
        //指针2 指向字符串
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);

            //跳过特殊符号
            if(isSymbol(c)){
                //若指针1处于跟节点,将此字符计入结果,让指针2走向下一步
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或中间,指针三都向下走一步
                position++;
                continue;
            }

            //不是符号,检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
            //没有该节点以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //指针2进入下一个位置,指针3和指针2保持同一个位置
                position = ++ begin;
                //指针1重新指向跟节点
                tempNode = rootNode;
            }else if (tempNode.isKeyWordEnd()){
             //发现了敏感词,将begin~position该段的字符串替换掉
                sb.append(REPLACEMENT);
                //进入到下一个位置
                begin = ++ position;
                //重新指向根节点
                tempNode = rootNode;
            }else {
                //继续检查下一个字符
                position++;
            }

        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();

    }

    /**
     * 判断是否为符号
     * @param c
     * @return
     */
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 前缀树
     */
    private class  TrieNode{

        //关键词结束表示
        private boolean isKeyWordEnd = false;

        //子节点  key是下级字符,value是下级节点
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character character){
            return subNodes.get(character);
        }

    }
}
