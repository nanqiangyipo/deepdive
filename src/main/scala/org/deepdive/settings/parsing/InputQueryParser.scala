package org.deepdive.settings

import scala.util.parsing.combinator.RegexParsers

object InputQueryParser extends RegexParsers {

  def filenameExpr =  "'" ~> """[\w\s/\.-]+""".r <~ "'"
  def CSVInputQueryExpr = "CSV" ~> "(" ~> filenameExpr <~ ")" ^^ { str => CSVInputQuery(str, ',') }
  def TSVInputQueryExpr = "TSV" ~> "("~> filenameExpr <~ ")" ^^ { str => CSVInputQuery(str, '\t') }
  def DatastoreInputQueryExpr = not("CSV") ~> not("TSV") ~> ".+".r ^^ { str => DatastoreInputQuery(str) }
  def inputQueryExpr = (CSVInputQueryExpr | TSVInputQueryExpr | DatastoreInputQueryExpr)

}
