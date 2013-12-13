package org.deepdive.test.unit

import com.typesafe.config._
import org.deepdive.settings._
import org.deepdive.context._
import org.scalatest._

class SettingsSpec extends FunSpec {

  def settingsFile = """
  deepdive {
    
    global.connection: {
      url: "jdbc:postgresql://localhost/deepdive_test"
      user: "deepdive"
      password: "password"
    }

    relations.documents.schema: { id: Integer, text: Text, meta: Text }
    relations.entities.schema: { id: Integer, document_id: Integer, name: String, meta: Text, is_present: Boolean}
    relations.entities.fkeys: { document_id: documents.id }

    extractions: {
      extractor1.output_relation: "entities"
      extractor1.input: "SELECT * FROM documents"
      extractor1.udf: "udf/entities.py"
    }

    factors: {
      entities.input_query = "SELECT documents.*, entities.* FROM documents INNER JOIN entities ON entities.document_id = documents.id"
      entities.function: "entities.is_present = Imply()"
      entities.weight: "?"
    }
  }
"""

  describe("Settings") {

    it("should parse a simple configuration file") {
      val settings = Settings.loadFromConfig(ConfigFactory.parseString(settingsFile))

      assert(settings.connection == Connection("jdbc:postgresql://localhost/deepdive_test",
        "deepdive", "password"))
      
      assert(settings.relations.toSet == Set(
        Relation(
          "documents", 
          Map[String,String]("id" -> "Integer", "text" -> "Text", "meta" -> "Text"), 
          List[ForeignKey](ForeignKey("documents","id","documents","id")),
          None),
        Relation("entities", 
          Map[String,String]("id" -> "Integer", "document_id" -> "Integer", 
            "name" -> "String", "meta" -> "Text", "is_present" -> "Boolean"), 
          List[ForeignKey](
            ForeignKey("entities","document_id","documents","id"),
            ForeignKey("entities","id","entities","id")),
          None)
      ))

      assert(settings.extractors == List(
        Extractor("extractor1", "entities", "SELECT * FROM documents", "udf/entities.py")
      ))

      assert(settings.factors == List(
        FactorDesc("entities", 
          """SELECT documents.*, entities.* FROM documents INNER JOIN entities ON entities.document_id = documents.id""",
          ImplyFactorFunction(FactorFunctionVariable("entities", "is_present"), Nil), 
          UnknownFactorWeight(Nil))
      ))

    }

  }

}