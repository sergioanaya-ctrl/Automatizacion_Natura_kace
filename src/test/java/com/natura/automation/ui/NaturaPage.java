package com.natura.automation.ui;

public class NaturaPage {

    // URL de login Cognito de Natura.
    // Pasar como propiedad de sistema: -Dnatura.cognito.url=https://...
    public static final String COGNITO_LOGIN_URL = System.getProperty(
            "natura.cognito.url",
            "https://us-east-1hbdyxxaze.auth.us-east-1.amazoncognito.com/login?client_id=22s3uck8lljk4u9uo15i21glvh&response_type=code&scope=email+openid+profile&redirect_uri=https://natura.krosscloud-konecta.com/auth");

    // URL del agente de Natura (pantalla principal tras login).
    // Pasar como propiedad de sistema: -Dnatura.agent.url=https://...
    public static final String AGENT_URL = System.getProperty(
            "natura.agent.url",
            "https://natura.krosscloud-konecta.com/agent");

    private NaturaPage() {}
}
