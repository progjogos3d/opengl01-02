# Aula 1 - Projeto completo

Neste projeto está a implementação do primeiro triângulo. Para que esse triângulo seja desenhado os seguintes passos 
foram necessários.

# Criação da malha

Neste exemplo, desenhamos um único triângulo, com base apenas nas suas posições. Este é um exemplo simples mas, na 
prática, malhas poligonais são bem mais complexas. Elas possuem várias propriedades associadas ao vértice além da 
posição como coordenadas de textura, normais de luz, etc. 

Como a OpenGL é uma biblioteca profissional para desenho, ela deve contemplar desde as malhas mais simples até as mais 
complexas. Por isso, alguns passos dessa demonstração irão parecer bastante complexos inicialmente.

Na OpenGL, uma malha é chamada de Vertex Array Object (VAO). Dentro dele, haverá um conjunto de buffers, cada um com uma
propriedade do vértice. No caso de nosso triangulo, teremos apenas um buffer contendo as posições dos vértices. 

Um buffer para opengl nada mais é do que um array na memória de vídeo.

É importante notar no código que tanto a malha, quanto os buffers serão enviados para a placa de vídeo por isso, no 
Java, eles serão identificados apenas por um identificador. Funciona assim:

 - Nós criamos a informação desejada no Java
 - Chamamos as funções apropriadas para enviar os dados para a placa
 - Biblioteca nos retorna um id, que é usado sempre que quisermos usar esse dado

Outro detalhe importante é lembrarmos que a OpenGL trabalha no sistema de coordenadas da mão direita. Por isso, as 
posições devem ser fornecidas no sentido *anti-horário* (se vc fornecer no sentido horário, o triângulo ficará de 
costas).

O sistema de coordenada padrão da OpenGL (chamado de coordenadas normalizadas) vai de 0 até 1 tanto em x, quanto em y. 
Sendo o (0,0) no centro da tela.

# Compilação dos shaders

Uma segunda tarefa importante no desenho é compilar os *shaders*. Shaders são programas enviados para a placa de vídeo 
para controlar parte do desenho. Existem 2 shaders obrigatórios:

* O Vertex shader: Que é executado uma vez para cada vértice desenhado. Seu principal objetivo é converter as 
coordenadas do vértice, que estão centralizadas na malha, para as coordenadas finais, já considerando posição no mundo e  
camera. Ele também pode processar e decidir quais informações serão enviadas para o Fragment Shader
* O Fragment shader: Que roda uma vez para cada pixel desenhado e calcula a cor final do pixel. Um fragment shader 
completo incluirá cálculo de iluminação, 

No caso desse exemplo, os shaders estão definidos em duas variáveis:

```java
	private static final String VERTEX_SHADER = 
			"#version 330\n" +
			"in vec2 aPosition;\n" + 
			"void main(){\n" + 
			"    gl_Position = vec4(aPosition, 0.0, 1.0);\n" + 
			"}";
	
	private static final String FRAGMENT_SHADER = 
			"#version 330\n" + 
			"out vec4 out_color;\n" + 
			"void main(){\n" + 
			"    out_color = vec4(1.0, 1.0, 0.0, 1.0);\n" + 
			"}";
```

Observe que o texto do shader em si, definido nas Strings, é somente:

```glsl
#version 330
in vec2 aPosition;
void main(){
    gl_Position = vec4(aPosition, 0.0, 1.0);
}
```
 
para o vertex shader e
 
```glsl
#version 330 
out vec4 out_color; 
void main(){ 
    out_color = vec4(1.0, 1.0, 0.0, 1.0); 
}
```

para o fragment shader. Não se preocupe em entender o funcionamento dos shaders agora, isso será foco de outras aulas. 

O importante é sabermos apenas que teremos que compilar e enviar esses programas para a placa. Além disso, é importante 
saber que o Vertex Shader contém uma variável chamada `aPosition` que deve ser associada ao buffer de posições antes do 
desenho. 

A título de curiosidade, essas são versões extremamente simples dos shaders. O Vertex Shader só repassa as coordenadas 
para a placa de vídeo e o fragment shader simplesmente retorna a cor amarela.

A compilação do shader é dividida em duas etapas:

- Compilação: Que é a interpretação do programa do shader na linguagem glsl. Nessa etapa, erros de sintaxe são 
detectados.
- Link: É quando dizemos qual vertex shader trabalhará com qual fragment shader. Nesta etapa, a OpenGL fará verificações
 como garantir que variáveis de saída do vertex shader estejam realmente presentes na entrada do fragment shader
 
Ao final do processo, a OpenGL gera um *shader program*, que será usado no desenho. Esse shader program também estará na 
placa de vídeo e, portanto, será identificado por um id.
 