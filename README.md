# **Blockchain Trabalho 2**

O objetivo principal deste trabalho, é implementar a lógica Scrooge, para processar e validar transações e desenvolver uma *public ledger*. O programa deve responder a essas necessidades, e passar a testes, que já vinham com a pasta do trabalho.

As classes implementadas, foram a ```TxHandler.java```, a ```Crypto.java```, a ```Main.java```, a ```Brute.java``` e a ```Greedy.java```.

## **A classe ```TxHandler.java```**

Esta é a classe central do projeto! Cá encontra-se a lógica Scrooge, com osseguintes métodos:

- **TxHandler(UTXOPool utxoPool)** - Constructor - Inicializa o handler, criando uma cópia defensiva da ```UTXOPool``` inicial. Esta cópia representa o estado atual de outputs não consumidos (gastos).
- **isValidTx(Transaction tx)** - Este método realiza todas as validações necessárias para avançar com qualquer transação:
    - Verifica se os inputs reivindicados - _UTXOs_ - estão presentes no atual ```utxoPool```.
    - Verifica se a assinatura digital em cada _UTXO_, utilizando um método da classe ```Crypto.java```, o ```Crypto.verifySignature```, para verificar que o utilizador está autoriizado a gastar.
    - Utiliza um set (```HashSet```) de _UTXOs_ reivindicadas, para que se verificque que estas não são reutilizadas na mesma transação.
    - Verifica se a soma dos valores de entrada são maiores do que os valores de saída, para prevenir inflação.
    - E por fim, verifica se os valores de saída não são negativos.
- **handleTxs(Transaction[] possibleTxs)** - Processa uma lista de transações propostas ao longo de um período, e devolve uma matriz mutamente válida de transações aceites de tamanho máximo. Itera, repetitivamente sobre as transações restantes, aceitando as que são válidas e utiliza o método ```applyTx()``` para atualizar imediatamente o pool. Isto considera as transações que fazem referência a saídas de outras transações dentro do mesmo bloco.
-  **applyTx** - Mencionado acima, este método atualiza imediatamente a `UTXOPool`, removendo as _UTXOs_ que foram gastas pela transação aceite, e adicionando as novas _UTXOs_, criadas pela transação.

- Por fim, foram incluídos métodos de leitura para que, durante o desenvolvimento, tivessemos facilidade a ver as transações e o `UTXOPool` claramente.

## **A classe ```Crypto.java```**

Esta classe já vinha implementada com o código inicial do trabalho, no entanto adicionou-se um método de assinatura necessário para a lógica da classe anterior.

- **verifySignature(PublicKey pubKey, byte[] message, byte[] signature)**: Esta é a função essencial que é utilizada no método anterior ```ìsValidTx```; é com este método que se verifica as assinaturas digitais, que para efeitos de teste são geradas pela função ```sign(PrivateKey privateKey, byte[] message)```

## **A classe ```Main.java```**
Aqui é mostrado como se utilizam as classes do projeto, descrevemos o processo de uma transação a utilizar a Scrooge! Neste ficheiro:
- São geradas chaves RSA para simular um utilizador.
- É gerada uma transação *unspent* no valor de 10 coins.
- Inicializasse `UTXOPool`, adicionando-lhe os valores da transição criada anteriormente.
- Criasse a transação, que pretende consumir o nosso input de 10 _coins_, e gerar 2 outputs com 5 _coins_ e 4 _coins_.
- Assinam-se os dados da transação de entrada com a chave privada gerada no inicio.
- Processa-se a transação, através da chamada de ```handler.handleTx()```com a nova transação.
- E por fim, imprime-se as transações aceites e o estado final da `UTXOPool`.


Na secção seguinte mostro o output do programa!


## **Output**

```bash
=== GENESIS CREATED ===
UTXO -> value: 10.0, owner: Sun RSA public key, 1024 bits
  params: null
  modulus: 146155733295685206170746277245177605216220746221764660753254257558744401828118958126033261379352806863310202232311924284129452323318903940202114837531905813224895465494118125351901182107427784248946076235659014468368595024806735944777178058132267597188393902923199288768924537369990784471730842050300024121999
  public exponent: 65537


##################################
##################################
##################################

--- Greedy Selection ---
Time Taken: 3.0842 ms
Selected txs: 6
Total Fee (Greedy): 6.5000

##################################
##################################
##################################

Current UTXO Pool:
  UTXO: 9c755452ecc08b6ad891dc7fe4312d1bdd8a73277ba98987c8ffcc2ab88abbaf | idx: 1 | value: 1.0
  UTXO: e64b41967ed9db97196b1ddd00b6cdb9e74b2c7a3d767d153066d5f336f6960e | idx: 0 | value: 2.0
  UTXO: b81c29d1f32b69403f683e1872b8d81988963536a14bfd0a4473d718eaf88300 | idx: 0 | value: 0.5

##################################
##################################
##################################

--- Brute Force (Max 2 Txs) ---
Time Taken: 0.5085 ms
Total Fee: 4.0

--- Brute Force (Max 3 Txs) ---
Time Taken: 0.0627 ms
Total Fee: 5.0

--- Brute Force (Max Subset / Power Set) ---
Time Taken: 0.1617 ms
Total Fee: 6.5

================ SUMMARY ================
Greedy Algo :   3.0842 ms
Brute (2)   :   0.5085 ms
Brute (3)   :   0.0627 ms
Brute (All) :   0.1617 ms
=========================================
```


## Execução e Testes do Projeto

O projeto foi configurado para ser executado num ambiente Java padrão, sendo compatível com _IDEs_ modernos como o _IntelliJ IDEA_ ou _Eclipse_. A estrutura do projeto inclui o código-fonte principal e uma suite de testes unitários abrangente.

1. **Executar a Simulação Principal**

    O ponto de entrada da aplicação encontra-se na classe `Main.java`. Este ficheiro contém uma simulação completa do ciclo de vida de transações na ScroogeCoin, desde a criação do bloco Genesis até à seleção de transações via algoritmos Greedy e Brute Force.

**Para executar o programa:**
  - Navegue até ao ficheiro `Main.java` na árvore do projeto.
  - Execute o método `main()` (normalmente através de _Right-Click_ > Run `Main.java`).
  - O resultado será exibido na consola, detalhando passo-a-passo o processamento das transações, as assinaturas digitais e a comparação de performance entre os algoritmos.

2. **Executar os Testes Unitários**

    Para garantir a robustez e a correção da lógica implementada (especialmente as validações criptográficas e a prevenção de _double-spending_), foram incluídos testes unitários na pasta `/test`.

**Para correr os testes:**
  - Localize a diretoria `/test` na estrutura do projeto.
  - Execute todos os testes contidos na pasta (através de _Right-Click_ na pasta > Run 'All Tests').
  - O _IDE_ apresentará um relatório com o estado de cada teste (_Pass/Fail_), cobrindo cenários como assinaturas inválidas, transações mal formadas e conflitos de `UTXO`.

## **A Classe `Brute.java`**

Esta classe auxiliar foi desenvolvida para explorar algoritmos de otimização baseados em Força Bruta (_Brute Force_). O seu objetivo é analisar o conjunto de transações aceites e identificar quais as combinações que maximizam o lucro total das taxas (_fees_), ignorando a eficiência temporal em prol da garantia matemática do melhor resultado possível.
A classe implementa três estratégias distintas:

- ```BruteF(UTXOPool pool, Transaction[] allTxs, TxHandler handler)``` - Este método procura o par de transações (2) que oferece a maior soma de taxas.
  
   Utiliza dois ciclos embutidos (_nested loops_) para verificar todas as combinações únicas de dois elementos ($O(N^2)$).
  
   Retorna um array com os valores das duas maiores taxas encontradas.

- ```BruteF_Three(TxHandler handler)``` - Uma extensão da lógica anterior, desenhada para encontrar o trio de transações (3) com o maior retorno.

  Implementa três ciclos embutidos para testar todas as combinações possíveis de três transações ($O(N^3)$).
  
  Retorna as três taxas que compõem a melhor combinação encontrada.

- ```BruteF_MaxAll(TxHandler handler)``` - O método mais complexo e robusto da classe. O seu objetivo é encontrar o subconjunto ideal (de qualquer tamanho) que maximiza a taxa total entre todas as transações aceites.

  Como o número de transações pode variar, este método gera o Conjunto das Partes (_Power Set_) — ou seja, todas as combinações possíveis de transações ($2^N$).
  Utiliza uma abordagem iterativa (_Cascading_) em vez de recursiva ou binária: começa com uma lista vazia e, para cada nova taxa, duplica a lista de subconjuntos existentes, adicionando a nova taxa a cada cópia.

  Embora garanta o resultado máximo absoluto, a sua complexidade é exponencial ($O(2^N)$), sendo viável apenas para conjuntos de dados pequenos.

