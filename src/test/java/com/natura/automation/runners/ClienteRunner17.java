package com.natura.automation.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/clients/crear_cliente.feature",
        glue = "com.natura.automation.stepdefinitions",
        tags = "@cliente17",
        snippets = CucumberOptions.SnippetType.UNDERSCORE
)
public class ClienteRunner17 {
}
