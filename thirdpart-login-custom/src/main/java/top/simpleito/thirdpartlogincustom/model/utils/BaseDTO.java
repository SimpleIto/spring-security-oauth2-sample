package top.simpleito.thirdpartlogincustom.model.utils;

public class BaseDTO<E> {
    private E data;
    private Integer code;
    private String msg;

    public BaseDTO(){}
    public BaseDTO(E data, Integer code, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
