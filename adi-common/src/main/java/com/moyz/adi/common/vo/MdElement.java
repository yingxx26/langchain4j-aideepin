package com.moyz.adi.common.vo;

public
// 自定义元素：文本或图片
record MdElement(Type type, String content) {
    public enum Type {TEXT, IMAGE}
}
