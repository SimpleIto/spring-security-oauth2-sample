package top.simpleito.thirdpartlogincustom.model.utils;

public class Response {
    public static <E> BaseDTO<E> ok(E data) {
        return new BaseDTO<>(data, 0, null);
    }

    public static <E> BaseDTO<E> error(Integer code, String msg) {
        return new BaseDTO<>(null, code, msg);
    }

    public static <E> BaseDTO<E> error(E data, Integer code, String msg) {
        return new BaseDTO<>(data, code, msg);
    }
}
