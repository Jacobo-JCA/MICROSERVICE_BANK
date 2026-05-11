package com.sofka.customers.cucumber.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.sofka.customers.cucumber",
        plugin = {"pretty", "json:target/cucumber.json"}
)
public class CucumberIntegrationTest {
}
