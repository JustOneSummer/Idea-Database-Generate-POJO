

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON工具类
 *
 * @author CaoXun
 * @date 2020/12/14 15:45 星期一
 */
public class JsonUtils {
    private static final String ERROR_LOG = "JSON错误！";
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    /**
     * JSON工具
     */
    private static final ObjectMapper OBJECT_MAPPER;

    private JsonUtils() {
    }

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS)
                //JSON 序列化移除 transient 修饰的 Page 无关紧要的返回属性(Mybatis Plus)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
                // 忽略未知属性，防止json字符串中存在，java对象中不存在对应属性的情况出现错误
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(new JavaTimeModule());
    }

    /**
     * 序列化Java Bean为JSON数据
     *
     * @param data Java Bean
     * @param <T>  类型
     * @return JSON数据
     */
    public static <T> String toJson(T data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("{}{}", ERROR_LOG, e.getMessage(), e);
        }
        throw new NullPointerException();
    }

    /**
     * 解析JSON文件返回Java Bean
     *
     * @param json   json数据
     * @param tClass 类型
     * @param <T>    类型T
     * @return Java Bean
     */
    public static <T> T parse(String json, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            log.error("{}{}{}", ERROR_LOG, e.getMessage(), e);
        }
        throw new NullPointerException();
    }

    /**
     * 解析JSON文件返回Java Bean-应对Java复杂类型的Bean
     *
     * @param json json数据
     * @param type 复杂类型
     * @param <T>  类型T
     * @return Java Bean
     */
    public static <T> T parse(String json, TypeReference<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("{}{}{}", ERROR_LOG, e.getMessage(), e);
        }
        throw new NullPointerException();
    }
}
