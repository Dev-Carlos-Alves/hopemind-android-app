# HopeMind — App Android

Versão mobile do **HopeMind**, construída a partir do projeto-modelo da disciplina (*ComposeCrudExemplo*): Kotlin + Jetpack Compose + Material 3 + Navigation, **Android 7.0 (API 24) ou superior**.

O app fala com a mesma API NestJS do site ([`hopemind`](https://github.com/Dev-Carlos-Alves/hopemind)), que hoje roda no [Render](https://hopemind-api.onrender.com) e guarda os dados no [Supabase](https://supabase.com) (PostgreSQL). Login, questionário, match e sessões são os mesmos dados: uma conta criada no app funciona no site e vice-versa.

---

## Como rodar

1. Abra `D:\hopemind_app` no **Android Studio** e espere o Gradle sincronizar.
2. Crie um emulador (Device Manager → Pixel, Android 14) e clique em **Run ▶**.
3. Entre com as contas de demonstração (senha `hopemind123`), que aparecem como atalhos na tela de login:
   - Paciente: `paciente@hopemind.local`
   - Psicólogo: `rafael@hopemind.local` (os 26 psicólogos do seed ficam no Recife)

Por padrão o app já fala com a API hospedada no Render — não precisa rodar nada localmente. Como é
plano gratuito, ela "dorme" depois de um tempo sem uso: a primeira chamada depois disso pode levar
uns 30-50s (o app espera até 60s antes de dar erro, então funciona, só demora essa primeira vez).

### Testando contra a API local

Rodando a API do repositório [`hopemind`](https://github.com/Dev-Carlos-Alves/hopemind) localmente
(`npm run dev`) em vez da hospedada, aponte o app pra ela:

- **Emulador:** toque em **Servidor da API** na tela de login (ou em Ajustes) e informe
  `http://10.0.2.2:3000/api/` — `10.0.2.2` é "o localhost do PC" visto de dentro do emulador.
- **Celular físico:** mesma rede Wi-Fi do PC, e informe `http://<IP-do-PC>:3000/api/` (descubra o
  IP com `ipconfig`). Libere a porta 3000 no firewall do Windows se necessário.

Também dá para fixar o endereço no build, em `local.properties` (nunca vai para o Git):
```properties
hopemind.apiUrl=http://10.0.2.2:3000/api/
```

> A pasta fica em `D:\hopemind_app` (sem acentos) porque o Android Gradle Plugin não compila em caminhos com caracteres como "ç" e "ã" no Windows.

---

## Telas e fluxo

```text
Splash → Boas-vindas (idioma) → Login / Cadastro
                                   │
      ┌────────────── Paciente ────┴──────────── Psicólogo ─────────────┐
      │ Home · Sessões · Matches · Ajustes     Home · Agenda · Perfil · Ajustes
      │   Matches
      │   ├─ Meu questionário (expande dentro da tela)
      │   ├─ 10 melhores matches + "Ver mais" (compatibilidade mais baixa)
      │   ├─ Ordenar por compatibilidade ou distância (CEP)
      │   ├─ Ver horários (agendamento)
      │   └─ Por que essa indicação? (componentes e pesos do match)
      └─ Questionário com resposta de risco → Tela de apoio (CVV 188, SAMU 192)
```

| Tela | Equivalente no site |
|---|---|
| Boas-vindas | painel da marca do login (e o "PreHome" do modelo) |
| Login / Cadastro | `/login`, `/registro` — máscaras de CPF, celular e CEP (endereço preenchido pela API), CRP para psicólogos |
| Home | `/inicio` — quem somos, como funciona, fatores do match, princípios, abordagens, canais de apoio |
| Matches | `/matches` — questionário expansível, 10 melhores + "ver mais", bairro e distância, agendamento |
| Perfil (psicólogo) | `/triagem` — endereço do consultório + questionário de atendimento |
| Sessões / Agenda | `/consultas` — próximas e anteriores, agrupadas por dia |
| Ajustes | `/configuracoes` — dados, endereço por CEP, aparência (claro/escuro), idioma, servidor |

---

## Estrutura (mesma organização do projeto-modelo)

```text
br.edu.novaroma.hopemind
├── data
│   ├── remote        Retrofit + OkHttp (cookies de sessão, renovação automática)
│   ├── repository    Auth, Triage, Match, Appointment
│   └── AppContainer  injeção de dependência manual
├── domain/model      modelos iguais ao JSON da API
├── presentation
│   ├── navigation    AppNavHost, rotas, barra de abas
│   ├── i18n          LanguageManager (PT-BR / EN / dispositivo)
│   ├── components    botões, campos, segmented control, listas, avatar...
│   └── screen        telas + ViewModels
└── ui/theme          cores, tipografia e tema (mesmos tokens do site)
```

- **Idiomas:** todos os textos da interface estão em `res/values/strings.xml` (inglês) e `res/values-pt-rBR/strings.xml` (português). Perguntas do questionário e motivos do match vêm da API, em português, como no site.
- **Aparência:** mesma paleta do site (sálvia, areia e pêssego do logo), com modo claro e escuro.

---

## Screenshots sem emulador

Os testes renderizam cada tela na JVM (Robolectric + Roborazzi) com dados iguais aos do seed:

```bash
./gradlew testDebugUnitTest --tests "*ScreenshotTest*"
```

As imagens saem em `app/build/outputs/roborazzi/`.
