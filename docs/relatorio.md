# Relatório Técnico: Concorrência e Sincronização com Semáforos em Transações Pix

**Instituição:** Universidade Federal do Pampa (UNIPAMPA)  
**Disciplina:** Sistemas Operacionais  
**Trabalho 1:** Threads e Semáforos  


**Aluno:** Fade H. H. Kanaan  
**Repositório:** [github.com/fadekanaan/threads-semaforos-pix](https://github.com/fadekanaan/threads-semaforos-pix)  

---

## 1. Introdução e Descrição da Aplicação

Sistemas operacionais contemporâneos operam em ambientes multiprocessados e preemptivos. Múltiplas linhas de execução (*threads*) compartilham o mesmo espaço de memória do processo (código, dados e *heap*), usufruindo da capacidade computacional de múltiplos núcleos de CPU. Entretanto, o compartilhamento de recursos sem sincronização adequada dá origem a **condições de corrida** (*race conditions*).

Neste trabalho, foi desenvolvido um simulador de movimentações financeiras via **Pix**, simulando uma conta bancária de saldo compartilhado submetida a alto volume de transações concorrentes de entrada (depósito/recebimento) e saída (saque/envio).

A aplicação foi estruturada na linguagem Java em quatro classes principais:
- [`Pix.java`](../application/Pix.java): Entidade responsável por armazenar o `saldo` compartilhado e prover métodos de débito e crédito, tanto na versão desprotegida quanto na versão sincronizada via `java.util.concurrent.Semaphore`.
- [`ClienteDepositante.java`](../application/ClienteDepositante.java): Implementa a interface `Runnable`, simulando clientes que recebem Pix consecutivamente.
- [`ClienteSacador.java`](../application/ClienteSacador.java): Implementa `Runnable`, simulando clientes que enviam Pix consecutivamente.
- [`Main.java`](../application/Main.java): Ponto de entrada que orquestra o experimento concorrente, colhe as métricas de tempo e verifica a integridade matemática do saldo final.

---

## 2. Fundamentação Teórica

### 2.1 A Condição de Corrida e a Não-Atomicidade
A operação básica de atualização de saldo em memória:
$$\text{saldo} \leftarrow \text{saldo} \pm \text{valor}$$
não é atômica no nível de instruções de máquina. Ela se decompõe internamente em três etapas:
1. **Leitura (Load):** O valor atual do saldo é copiado da memória RAM para um registrador interno da CPU.
2. **Modificação (ALU):** A soma ou subtração é efetuada no registrador.
3. **Escrita (Store):** O novo valor computado é gravado de volta na memória RAM.

Em um ambiente com preempção e múltiplos núcleos de CPU, o escalonador do sistema operacional pode suspender uma thread no instante exato entre o *load* e o *store*. Se outra thread modificar o saldo nesse intervalo, a primeira thread, ao retomar a execução, sobrescreverá o valor na RAM com base em um dado desatualizado. Esse fenômeno é denominado **atualização perdida (*lost update*)**.

### 2.2 Semáforos de Dijkstra e Exclusão Mútua
Para mitigar esse problema, foi empregado o conceito clássico de **Semáforos**, proposto por Edsger Dijkstra. Um semáforo é uma estrutura de sincronização composta por um contador inteiro e uma fila de processos adormecidos, controlada por duas primitivas atômicas:
- **$P()$ / *Down* / `acquire()`:** Decrementa o contador de permissões. Se o contador for menor ou igual a zero, a thread solicitante é suspensa na fila de espera do sistema operacional, sem desperdício de ciclos de processamento por espera ativa (*busy waiting*).
- **$V()$ / *Up* / `release()`:** Incrementa o contador de permissões e sinaliza para o escalonador acordar uma das threads suspensas.

Nesta aplicação, utiliza-se um **semáforo binário** (inicializado com $1$ permissão), o qual atua estritamente como um **Mutex**, delimitando a **seção crítica** onde o saldo compartilhado é acessado e modificado.

---

## 3. Metodologia e Teste Implementado

Para demonstrar empiricamente a falha de exclusão mútua e a subsequente eficácia dos semáforos, foi configurado o seguinte teste de estresse concorrente:

- **Saldo Inicial da Conta:** R$ 1.000,00
- **Threads Depositantes:** 5 threads
- **Threads Sacadoras:** 5 threads
- **Transações por Thread:** 10.000 operações
- **Valor por Operação:** R$ 10,00
- **Volume Total Movimentado:**
  - Depósitos: 5 × 10.000 × R$ 10,00 = R$ 500.000,00
  - Saques: 5 × 10.000 × R$ 10,00 = R$ 500.000,00
- **Saldo Final Esperado:** R$ 1.000,00 + R$ 500.000,00 - R$ 500.000,00 = **R$ 1.000,00**

O experimento foi executado sob duas condições idênticas de carga:
1. **Cenário 1 (Sem Semáforo):** Acesso simultâneo direto à variável de saldo.
2. **Cenário 2 (Com Semáforo):** Acesso intermediado por `semaforo.acquire()` e `semaforo.release()`.

Em ambos os cenários, o tempo total de execução foi mensurado entre o disparo simultâneo das threads (`start()`) e a conclusão da última thread participante via sincronização de barreira com `join()`.

---

## 4. Resultados Obtidos e Discussão

Os testes foram executados em ambiente Linux (Ubuntu sobre WSL2) com Java OpenJDK 21 (64-Bit Server VM). Para evidenciar a natureza não-determinística da condição de corrida, o experimento foi executado repetidas vezes. Os dados colhidos em cinco rodadas consecutivas estão sintetizados na tabela a seguir:

| Rodada | Modo Sem Semáforo (Saldo Final) | Erro / Discrepância | Tempo (Sem Semáforo) | Modo Com Semáforo (Saldo Final) | Erro / Discrepância | Tempo (Com Semáforo) |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **#1** | R$ 31.340,00 | +R$ 30.340,00 | 4 ms | **R$ 1.000,00** |  **R$ 0,00** | 13 ms |
| **#2** | R$ 17.640,00 | +R$ 16.640,00 | 3 ms | **R$ 1.000,00** | **R$ 0,00** | 11 ms |
| **#3** | -R$ 36.490,00 | -R$ 37.490,00 | 4 ms | **R$ 1.000,00** | **R$ 0,00** | 11 ms |
| **#4** | R$ 8.940,00 | +R$ 7.940,00 | 3 ms | **R$ 1.000,00** | **R$ 0,00** | 12 ms |
| **#5** | -R$ 73.830,00 | -R$ 74.830,00 | 3 ms | **R$ 1.000,00** | **R$ 0,00** | 12 ms |

### Análise dos Resultados:
1. **Não-Determinismo e Condição de Corrida (Cenário 1):** Como observado na tabela ao longo de cinco execuções consecutivas sem semáforo, o saldo final atinge valores completamente dispersos e imprevisíveis: varia de um saldo positivo inflado de +R$ 31.340,00 até um déficit extremo de -R$ 73.830,00 (um erro de quase R$ 75 mil em relação aos R$ 1.000,00 esperados). Isso comprova a violação flagrante da exclusão mútua em razão da preempção das threads e das instruções não-atômicas de leitura e escrita.
2. **Corretude e Determinismo com Semáforo (Cenário 2):** Em todas as 5 rodadas, o semáforo binário garantiu que cada atualização de saldo ocorresse de forma estritamente isolada e serializada. O saldo final bateu com precisão matemática em **R$ 1.000,00 (erro R$ 0,00)** em todas as baterias de teste.
3. **Custo Computacional (*Overhead*):** O tempo médio de execução sem semáforo foi de ~3,4 ms, enquanto com semáforo subiu para ~11,8 ms — sendo cerca de 3,5 vezes maior (um acréscimo de aproximadamente 250% no tempo de execução). Esse tempo adicional decorre das operações de sincronização, verificação atômica de permissões e do escalonamento de threads na fila do semáforo. Embora percentualmente significativo, na prática bancária essa sobretaxa de alguns milissegundos é essencial para evitar perdas financeiras catastróficas.

---

## 5. Conclusão

O experimento cumpriu integralmente os objetivos propostos no Trabalho 1:
- Demonstrou-se de maneira reproduzível a existência de condições de corrida em aplicações multithread em ambiente multiprocessado.
- Provou-se que primitivas de semáforos resolvem o problema ao garantir a propriedade de **Exclusão Mútua** proposta por Dijkstra.
- Evidenciou-se a relação entre segurança de dados e custo de sincronização, consolidando os conceitos de seção crítica, preempção e coordenação entre processos leves (threads).
