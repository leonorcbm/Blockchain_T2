# **Blockchain Trabalho 2**

O objetivo principal detste trabalho, é implementar a lógica Scrooge, para processar e validar transações e desenvolver uma *public ledger*. O programa deve responder a essas necessidades, e passar a testes, que já vinham com a pasta do trabalho.

As classes implementadas, foram a ```TxHandler.java```, a ```Crypto.java``` e a ```Main.java```.

## **A classe ```TxHandler.java```**

Esta é a classe central do projeto! Cá encontra-se a lógica Scrooge, com osseguintes métodos:

- **TxHandler(UTXOPool utxoPool)** - Constructor - Inicializa o handler, criando uma cópia defensiva da ```UTXOPool``` inicial. Esta cópia representa o estado atual de outputs não consumidos (gastos).
- **isValidTx(Transaction tx)** - Este método realiza todas as validações necessárias para avançar com qualquer transação:
    - Verifica se os inputs reivindicados - UTXOs - estão presentes no atual ```utxoPool```.
    - Verifica se a assinatura digital em cada UTXO, utilizando um método da classe ```Crypto.java```, o ```Crypto.verifySignature```, para verificar que o utilizador está autoriizado a gastar.
    - Utiliza um set (```HashSet```) de UTXOs reivindicadas, para que se verificque que estas não são reutilizadas na mesma transação.
    - Verifica se a soma dos valores de entrada são maiores do que os valores de saída, para prevenir inflação.
    - E por fim, verifica se os valores de saída não são negativos.
- **handleTxs(Transaction[] possibleTxs)** - Processa uma lista de transações propostas ao longo de um período, e devolve uma matriz mutamente válida de transações aceites de tamanho máximo. Itera, repetitivamente sobre as transações restantes, aceitando as que são válidas e utiliza o método ```applyTx``` para atualizar imediatamente o pool. Isto considera as transações que fazem referência a saídas de outras transações dentro do mesmo bloco.
-  **applyTx** - Mencionado acima, este método atualiza imediatamente a UTXOPool, removendo as UTXOs que foram gastas pela transação aceite, e adicionando as novas UTXOs, criadas pela transação.

- Por fim, foram incluídos métodos de leitura para que, durante o desenvolvimento, tivessemos facilidade a ver as transações e o UTXOPool claramente.

## **A classe ```Crypto.java```**

Esta classe já vinha implementada com o código inicial do trabalho, no entanto adicionou-se um método de assinatura necessário para a lógica da classe anterior.

- **verifySignature(PublicKey pubKey, byte[] message, byte[] signature)**: Esta é a função essencial que é utilizada no método anterior ```ìsValidTx```; é com este método que se verifica as assinaturas digitais, que para efeitos de teste são geradas pela função ```sign(PrivateKey privateKey, byte[] message)```

## **A classe ```Main.java```**
Aqui é mostrado como se utilizam as classes do projeto, descrevemos o processo de uma transação a utilizar a Scrooge! Neste ficheiro:
- São geradas chaves RSA para simular um utilizador.
- É gerada uma transação *unspent* no valor de 10 coins.
- Inicializasse **UTXOPool**, adicionando-lhe os valores da transição criada anteriormente.
- Criasse a transação, que pretende consumir o nosso input de 10 coins, e gerar 2 outputs com 5 coins.
- Assinam-se os dados da transação de entrada com a chave privada gerada no inicio.
- Processa-se a transação, através da chamada de ```handler.handleTx()```com a nova transação.
- E por fim, imprime-se as transações aceites e o estado final da **UTXOPool**.


Na secção seguinte mostro o output do programa!


## **Output**

```bash
=== GENESIS CREATED ===
UTXO -> value: 10.0, owner: Sun RSA public key, 1024 bits
  params: null
  modulus: 96936567253961636360015319696588896557431047711844175315307494385662010021508058118149769165844233616850864598325185573238523239546680447676801517350244823897382627980603250338979499865181919829988028515410842641699175672320978275317963603985075755066946715271612967199804985857320274788188474786461497392963
  public exponent: 65537

=== PROCESSING TRANSACTION ===
Inputs: 1
Outputs: 2 (5.0, 5.0)

-------------------------------------------------
Transaction ad2d45516e60cc049f2ea6676f6f90faad4d7dcdd0732c444818fc461e4fa67d
Inputs:
  - prevTx: 5ef648b51e18496b7f5309089c5c29484df687ac07a846a253fa9f0e32adcdeb | outputIndex: 0
Outputs:
  - value: 5.0 | address: Sun RSA public key, 1024 bits
  params: null
  modulus: 96936567253961636360015319696588896557431047711844175315307494385662010021508058118149769165844233616850864598325185573238523239546680447676801517350244823897382627980603250338979499865181919829988028515410842641699175672320978275317963603985075755066946715271612967199804985857320274788188474786461497392963
  public exponent: 65537
  - value: 5.0 | address: Sun RSA public key, 1024 bits
  params: null
  modulus: 96936567253961636360015319696588896557431047711844175315307494385662010021508058118149769165844233616850864598325185573238523239546680447676801517350244823897382627980603250338979499865181919829988028515410842641699175672320978275317963603985075755066946715271612967199804985857320274788188474786461497392963
  public exponent: 65537
Current UTXO Pool:
  UTXO: ad2d45516e60cc049f2ea6676f6f90faad4d7dcdd0732c444818fc461e4fa67d | idx: 1 | value: 5.0
  UTXO: ad2d45516e60cc049f2ea6676f6f90faad4d7dcdd0732c444818fc461e4fa67d | idx: 0 | value: 5.0
=== RESULTS ===
Accepted transactions: 1
Final UTXO pool entries:
 - Hash=5ef648b51e18496b7f5309089c5c29484df687ac07a846a253fa9f0e32adcdeb Index=0 Value=10.0

```

## **Correr o projeto e os testes**
Not sure se este é um passo que o intellij n faz sozinho mas right click no ficheiro ```pom.xml```, e clicar na ultima opção que fala em Maven e ta.

Correr é fácil! Basta correr o ficheiro ```Main.java```.

Para correr os testes basta fazer right click na pasta de ```/test```, e correr todos os testes.

## **O que falta fazer**
A análise empírica : testes brute force e greedy.
E eventualmente ajustar o output para ficar mais bonito. Sorry tive que ir vendo as coisas.
