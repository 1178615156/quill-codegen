package gen

trait NameMapping {


  def tableName2className(s: String): String

  def columnName2fieldName(s: String): String

  /** the variableName is :
    * val [[variableName]] = quote(querySchema[Class]())
    */
  def className2variableName(className: String): String

  //help
  def headLower(s: String) = s.head.toLower + s.tail

  def headUpper(s: String) = s.head.toUpper + s.tail
}

object NameMapping {

  trait Default extends NameMapping {
    def column2fieldLiteral = false

    def tableName2className(s: String) = s.split("_").toList.map(headUpper).mkString("")

    def columnName2fieldName(s: String) = {
      val name: String = if(column2fieldLiteral) s else headLower(s.split("_").toList.map(headUpper).mkString(""))
      name match {
        case "class"  => s"`${name}`"
        case "object" => s"`${name}`"
        case "type"   => s"`${name}`"

        case name if name.forall(e => e.isLetterOrDigit || e == '_') => name

        case _ => s"`${name}`"
      }

    }


    def className2variableName(s: String) = headLower(s)
  }

}