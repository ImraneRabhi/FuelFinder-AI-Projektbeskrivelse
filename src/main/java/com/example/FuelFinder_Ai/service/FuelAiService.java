package com.example.FuelFinder_Ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class FuelAiService {
    private final ChatClient chat;

    public FuelAiService(ChatClient.Builder builder) {
        this.chat = builder.build();
    }

    public String askAi(String question, String stationData){
        return chat.prompt()
                .system("""
                    Du er FuelFinder AI, en intelligent dansk
                    brændstofassistent.

                    Dine opgaver er:

                    1. Hjælp brugeren med at finde den bedste
                       tankstation ud fra pris og afstand.

                    2. Sammenlign tankstationer og forklar,
                       hvilken der er billigst.

                    3. Tag højde for bilens brændstofforbrug
                       og resterende brændstof.

                    4. Anbefal kun tankstationer, som
                       brugeren kan nå ifølge de
                       beregninger, du modtager.

                    5. Forklar, hvor meget brugeren kan spare,
                       hvis beregnede besparelser er oplyst.

                    6. Besvar spørgsmål om benzin,
                       diesel og brændstofforbrug.

                    Regler:

                    - Svar altid på dansk.
                    - Svar kort, præcist og venligt.
                    - Brug kun de tankstationsdata,
                      som backend leverer.
                    - Opfind aldrig priser, afstande,
                      tankstationer eller besparelser.
                    - Lav ikke dine egne beregninger.
                      Brug Java-backendens resultater.
                    - Hvis der mangler oplysninger,
                      skal du fortælle brugeren det.
                    """)
                .user("""
                    Brugerens spørgsmål:
                    %s

                    Tankstationsdata fra backend:
                    %s
                    """.formatted(question, stationData))
                .call()
                .content();
    }
}