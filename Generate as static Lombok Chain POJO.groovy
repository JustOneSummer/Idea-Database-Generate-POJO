import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*
 * idea 生成类 集成 mybatis-plus lombok json工具类
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 *   author https://linxun.link
 */

typeMapping = [
        (~/(?i)int/)                      : "Integer",
        (~/(?i)long/)                     : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/)       : "java.sql.Timestamp",
        (~/(?i)date/)                     : "java.sql.Date",
        (~/(?i)time/)                     : "java.sql.Time",
        (~/(?i)/)                         : "String"
]

//生成类名称
USER_NAME = "LinXun"
//邮箱
EMAIL = "me@linxun.link"
//JSON包路径
JSON_UTILS_PACKAGE = ""
LOCAL_DATE_TIME = LocalDateTime.now(ZoneId.systemDefault())
WEEK_DAY = LOCAL_DATE_TIME.getDayOfWeek().value
DATE_TIME = LOCAL_DATE_TIME.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
DAY_NAME_FULL = day_name_full()
TIME = LOCAL_DATE_TIME.format(DateTimeFormatter.ofPattern("HH:mm"))

def day_name_full() {
    String[] weeks = ["\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94", "\u516d", "\u65e5"]
    return "\u661f\u671f" + weeks[WEEK_DAY - 1]
}

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName(), true) + "DO"
    def sqlTableName = table.getName()
    def sqlTableComment = table.getComment()
    def fields = calcFields(table)
    new File(dir, className + ".java").withPrintWriter("UTF-8") { out -> generate(out, className, sqlTableComment, sqlTableName, fields) }
}

def generate(out, className, sqlTableComment, sqlTableName, fields) {
    out.println ""
    out.println "import lombok.Getter;"
    out.println "import lombok.Setter;"
    out.println "import lombok.ToString;"
    out.println "import lombok.experimental.Accessors;"
    out.println "import com.baomidou.mybatisplus.annotation.IdType;"
    out.println "import com.baomidou.mybatisplus.annotation.TableId;"
    out.println "import com.baomidou.mybatisplus.annotation.TableName;"
    out.println "import om.baomidou.mybatisplus.core.conditions.query.QueryWrapper;"
    if (JSON_UTILS_PACKAGE != "") {
        out.println "import $JSON_UTILS_PACKAGE"
    }
    out.println ""
    out.println "/**"
    out.println " * $sqlTableComment"
    out.println " * "
    out.println " * @author $USER_NAME"
    out.println " * @date $DATE_TIME $DAY_NAME_FULL $TIME"
    out.println " * @version 0.0.1"
    out.println " */"
    out.println "@Getter"
    out.println "@Setter"
    out.println "@ToString"
    out.println "@Accessors(chain = true)"
    out.println "@TableName(value = \"$sqlTableName\")"
    out.println "public class $className {"
    out.println ""
    for (i in 0..fields.size() - 1) {
        it = fields.get(i)
        if (it.annos != "") {
            out.println "  ${it.annos}"
        } else {
            if (i == 0) {
                out.println "    /**"
                out.println "     * ${it.comment}"
                out.println "     */"
                out.println "    @TableId(type = IdType.NONE)"
                out.println "    private ${it.type} ${it.name};"
            } else {
                out.println "    /**"
                out.println "     * ${it.comment}"
                out.println "     */"
                out.println "    private ${it.type} ${it.name};"
            }
        }
    }
    out.println ""
    out.println "    /**"
    out.println "     * No-parameter construction"
    out.println "     */"
    out.println "    public $className() {"
    out.println "    }"
    out.println ""
    if (JSON_UTILS_PACKAGE != "") {
        out.println "    /**"
        out.println "     * to json String"
        out.println "     */"
        out.println "    public String toJson() {"
        out.println "        return JsonUtils.toJson(this);"
        out.println "    }"
    }
    out.println "    }"
    out.println ""
    out.println "    /**"
    out.println "     * to {@linkplain QueryWrapper}"
    out.println "     *"
    out.println "     * @return {@linkplain QueryWrapper<${className}>}"
    out.println "     */"
    out.println "    public static QueryWrapper<${className}> to${className}() {"
    out.println "        return new QueryWrapper<>(this);"
    out.println "    }"
    out.println ""
    out.println "    /**"
    out.println "     * static Construct"
    out.println "     *"
    out.println "     * @return class"
    out.println "     */"
    out.println "    public static $className builder() {"
    out.println "        return new $className();"
    out.println "    }"
    out.println ""
    fields.each() {
        if (it.annos != "") out.println "  ${it.annos}"
        out.println "    /**"
        out.println "     * ${it.comment}"
        out.println "     */"
        out.println "    public static final String SQL_DB_${it.sqlKey} = \"${it.sqlName}\";"
    }
    out.println ""
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name   : javaName(col.getName(), false),
                           type   : typeStr,
                           annos  : "",
                           comment: col.getComment(),
                           sqlKey : javaStaticName(col.getName()),
                           sqlName: col.getName()]]
    }
}

def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

def javaStaticName(str) {
    char[] c = str.toCharArray();
    StringBuilder builder = new StringBuilder()
    for (char d : c) {
        if (Character.isUpperCase(d)) {
            builder.append("_")
        }
        builder.append(String.valueOf(d).toUpperCase(Locale.ROOT))
    }
    return builder.toString();
}
