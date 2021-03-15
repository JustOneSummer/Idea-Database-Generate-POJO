import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*
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

LOCAL_DATE_TIME = LocalDateTime.now(ZoneId.systemDefault())
WEEK_DAY = LOCAL_DATE_TIME.getDayOfWeek().value
DATE_TIME = LOCAL_DATE_TIME.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
DAY_NAME_FULL = day_name_full()
TIME = LOCAL_DATE_TIME.format(DateTimeFormatter.ofPattern("HH:mm"))
USER_NAME = "LinXun"

def day_name_full() {
    String[] weeks = ["\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94", "\u516d", "\u65e5"]
    return "\u661f\u671f" + weeks[WEEK_DAY - 1]
}

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName(), true) + "DO"
    def fields = calcFields(table)
    new File(dir, className + ".java").withPrintWriter("UTF-8") { out -> generate(out, className, fields) }
}

def generate(out, className, fields) {
    out.println ""
    out.println "import lombok.Getter;"
    out.println "import lombok.Setter;"
    out.println "import lombok.ToString;"
    out.println "import lombok.RequiredArgsConstructor;"
    out.println "import lombok.experimental.Accessors;"
    out.println ""
    out.println "/**"
    out.println " * @author $USER_NAME"
    out.println " * @date $DATE_TIME $DAY_NAME_FULL $TIME"
    out.println " * @version 0.0.1"
    out.println " */"
    out.println "@Getter"
    out.println "@Setter"
    out.println "@ToString"
    out.println "@Accessors(chain = true)"
    out.println "@RequiredArgsConstructor(staticName = \"of\")"
    out.println "public class $className {"
    out.println ""
    fields.each() {
        if (it.annos != "") out.println "  ${it.annos}"
        out.println "    private ${it.type} ${it.name};"
    }
    out.println ""
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
                           type : typeStr,
                           annos: ""]]
    }
}

def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
