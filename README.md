# threads-semaforos-pix

Simulador de transações Pix concorrentes desenvolvido para demonstrar **condições de corrida (*race conditions*)** e sincronização de threads utilizando **semáforos de Dijkstra**.

Projeto acadêmico desenvolvido para a disciplina de **Sistemas Operacionais** da Universidade Federal do Pampa (**UNIPAMPA**).

---

## 📌 Como Executar

Compile e execute o simulador diretamente via terminal (ou do jeito que preferir pela sua IDE):

```bash
# Compilar todas as classes
javac application/*.java

# Executar o experimento comparativo
java application.Main
```

---

## 📂 Estrutura do Projeto

- `application/`: Código-fonte Java contendo a conta Pix (`Pix.java`), os clientes concorrentes (`ClienteDepositante.java` e `ClienteSacador.java`) e a classe principal de experimentos (`Main.java`).
- `docs/`: Documentação do trabalho.
  - [`docs/relatorio.md`](docs/relatorio.md): Relatório técnico completo com fundamentação teórica, metodologia, tabela com as 5 rodadas de testes e análise dos resultados.

---

## 📄 Relatório Técnico

Para a fundamentação teórica sobre exclusão mútua, preempção de processos, primitivas `acquire()`/`release()` e os dados comparativos de execução, consulte o [Relatório Técnico](docs/relatorio.md).
