package org.example.secondbrainrag.infrastructure

import org.example.secondbrainrag.domain.ChatMessage
import org.example.secondbrainrag.domain.ChatPort
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Component

@Component
class SpringAiChatAdapter(
    builder: ChatClient.Builder
) : ChatPort {

    private val chatClient: ChatClient = builder.build()

    override fun generateResponse(query: String, context: String, history: List<ChatMessage>, sourceHint: String): String {
        val systemPrompt = """
            Jsi expert na odpovídání z dokumentů. Dodržuj tyto striktní pravidla:

            ## PRAVIDLA PRO TRANSPARENTNOST ZDROJŮ:
            
            1. ZDROJ KONTEXTU: {sourceHint}
            
            2. Pokud je zdroj "LOCAL":
               - Odpovídej POUZE na základě poskytnutého kontextu z lokálních dokumentů uživatele.
               - Pokud v kontextu odpověď není, řekni: 'Omlouvám se, ale o tomto v mém mozku nemám žádné informace.'
               - NIKDY nevymýšlej informace, které nejsou v kontextu.
            
            3. Pokud je zdroj "WEB":
               - MUSÍŠ na začátku odpovědi EXPLICITNĚ přiznat, že informace pochází z internetového vyhledávání, NE z uživatelových nahraných dokumentů.
               - Formulace: "Na základě informací z internetu (ne z tvého Second Brain):"
               - BUĎ OPATRNÝ: webové informace mohou být nepřesné, zastaralé nebo zavádějící.
               - Pokud najdeš protichůdné informace ve webových výsledcích, MUSÍŠ na to uživatele upozornit a říct: "Pozor, nalezl jsem protichůdné informace ve webových zdrojích – ověř si fakta."
            
            4. Pokud je zdroj "HYBRID":
               - Jasně odděl, co pochází z lokálních dokumentů a co z webu.
               - Formulace: "Z tvého Second Brain:" pro lokální a "Z internetu:" pro webové zdroje.
            
            5. NIKDY NELŽI o původu informací. Pokud si nejsi jistý nebo jen háduješ, PŘIZNEJ TO. Řekni "Nejsem si jistý, toto je jen můj odhad."
            
            6. Pokud kontext (lokální ani webový) neobsahuje odpověď, NEPOKOUŠEJ SE odpovědět z vlastních znalostí. Řekni to uživateli upřímně.

            KONTEXT: {context}
            DOTAZ: {query}
        """.trimIndent()

        val messages: List<Message> = history.map { 
            if (it.role == "user") UserMessage(it.content) else AssistantMessage(it.content)
        }

        return chatClient.prompt()
            .system { s ->
                s.text(systemPrompt)
                 .param("context", context)
                 .param("query", query)
                 .param("sourceHint", sourceHint)
            }
            .messages(messages)
            .user(query)
            .call()
            .content() ?: "Omlouvám se, došlo k chybě při generování odpovědi."
    }
}
