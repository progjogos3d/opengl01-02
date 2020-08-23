package br.pucpr.cg;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Window;

/**
 * Essa classe demonstra como desenhar um triangulo na tela utilizando a OpenGL.
 */
public class Triangle implements Scene {
    private Keyboard keys = Keyboard.getInstance();

	/**
	 * A variável VERTEX_SHADER_CODE contém o vertex shader usado no desenho.
	 * Este vertex shader somente repassa as coordenadas do vértice para a placa de vídeo, sem alterá-las.
	 */
	private static final String VERTEX_SHADER_CODE =
		"#version 330\n" +
		"in vec2 aPosition;\n" +
		"void main(){\n" +
		"    gl_Position = vec4(aPosition, 0.0, 1.0);\n" +
		"}";

	/**
	 * A variável FRAGMENT_SHADER_CODE contém o fragment shader usado no exemplo.
	 * Este fragment shader retorna a cor amarela.
	 */
	private static final String FRAGMENT_SHADER_CODE =
		"#version 330\n" +
		"out vec4 out_color;\n" +
		"void main(){\n" +
		"    out_color = vec4(1.0, 1.0, 0.0, 1.0);\n" +
		"}";

	/** Esta variável guarda o identificador da malha (Vertex Array Object) do triângulo */
	private int vao;

	/** Guarda o id do buffer com todas as posições do vértice. */
	private int positions;

	/** Guarda o id do shader program, após compilado e linkado */
	private int shader;

	//---------------------
	//Compilação do shader
	//---------------------

	/**
	 * Esta função compila o código do shader. Ao final da compilação, será gerado um id do shader compilado.
	 */
	private int compileShader(int shaderType, String code) {
		//Solicitamos a placa de vídeo um novo id de shader
		var shader = glCreateShader(shaderType);

		//Informamos a OpenGL qual é o código fonte do shader a ser compilado (variável code)
		glShaderSource(shader, code);

		//Solicitamos que a OpenGL faça a compilação.
		glCompileShader(shader);

		//Testamos se não houve erro de compilação
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
			//Caso haja, disparamos uma exceção informando o erro
			throw new RuntimeException("Unable to compile shader." + glGetShaderInfoLog(shader));
		}

		//Caso não haja, retornamos o id do shader
		return shader;
	}

	/**
	 * Une um vertex e um fragment shader, gerando o shader program que será usado no desenho.
	 * O parâmetro de entrada dessa função é um array com o id de todos os shaders que devem ser unidos.
	 */
	private int linkProgram(int... shaders) {
		//Solicitamos a criação de um id para o program
		var program = glCreateProgram();

		//Para cada shader recebido
		for (var shader : shaders) {
			//Informamos a OpenGL que ele está associado a esse program
			glAttachShader(program, shader);
		}

		//Solicitamos a linkagem
		glLinkProgram(program);

		//Testamos se não houve erro de link
		if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
			//Caso haja, disparamos uma exceção informando o erro.
			throw new RuntimeException("Unable to link shaders." + glGetProgramInfoLog(program));
		}

		//Caso contrário o program já está gerado. Uma vez pronto, não é necessário manter os shaders
		//usados no processo de geração na memória. Por isso, desassociamos eles e mandamos exclui-los.
		//Os shaders são apenas um passo intermediário na geração do program.
		for (var shader : shaders) {
			glDetachShader(program, shader);
			glDeleteShader(shader);
		}

		//Retornamos o id do shader program.
		return program;
	}

	@Override
	public void init() {
		//Define a cor de limpeza da tela
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		//------------------------------
		//Carga/Compilação dos shaders
		//------------------------------

		//Compila o vertex shader
		var vertex = compileShader(GL_VERTEX_SHADER, VERTEX_SHADER_CODE);

		//Compila o fragment shader
		var fragment = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

		//Une os dois num shader program
		shader = linkProgram(vertex, fragment);

		//------------------
		//Criação da malha
		//------------------

		//O processo de criação da malha envolve criar um Vertex Array Object e associar a ele um buffer, com as
		// posições dos vértices do triangulo.

		//Criação do Vertex Array Object (VAO)
		vao = glGenVertexArrays();

		//Informamos a OpenGL que iremos trabalhar com esse VAO
		glBindVertexArray(vao);

		//Criamos um array no java com as posições. Você poderia ter mais de um triângulo nesse mesmo
		//array. Para isso, bastaria definir mais posições.
		var vertexData = new float[] {
			     0.0f,  0.5f, 
			    -0.5f, -0.5f, 
			     0.5f, -0.5f 
		};

		//Solicitamos a criação de um buffer na OpenGL, onde esse array será guardado
		positions = glGenBuffers();
		//Informamos a OpenGL que iremos trabalhar com esse buffer
		glBindBuffer(GL_ARRAY_BUFFER, positions);

		//Damos o comando para carregar esses dados na placa de vídeo
		//o parametro GL_STATIC_DRAW indica que não mexeremos mais nos valores desses dados em nossa aplicação
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

		//---------------------------------
		//Vinculação da malha com o shader
		//--------------------------------

		//Agora, precisamos associar nosso buffer de posição a variável aPosition, definida dentro do shader.
		// para isso, localizamos dentro do shader o id da variável aPosition.
		var aPosition = glGetAttribLocation(shader, "aPosition");

		//Chamamos uma função que associa as duas. Essa função espera como parâmetro
		// - index: O id da variável sendo associada (aPosition)
		// - size: De quantos em quantos valores devem ser lidos. Observe que a variável é do tipo vec2, portanto,
		//são lidos de 2 em 2 floats.
		// - type: O tipo de dado do buffer. No caso, float

		// Os valores de normalized, stride e pointer serão sempre false, 0 e 0. Eles são usados caso você queira
		// criar um único buffer com vários atributos ao mesmo tempo (interlaced buffer), o que não faremos nas aulas.
		glVertexAttribPointer(aPosition, 2, GL_FLOAT, false, 0, 0);

		//Informamos a OpenGL que iremos trabalhar com essa variável
		glEnableVertexAttribArray(aPosition);

		//Como já finalizamos a carga, informamos a OpenGL que não estamos mais usando esse buffer.
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		//Finalizamos o nosso VAO, portanto, informamos a OpenGL que não iremos mais trabalhar com ele
		glBindVertexArray(0);


	}

	@Override
	public void update(float secs) {
		//Testa se a tecla ESC foi pressionada
        if (keys.isPressed(GLFW_KEY_ESCAPE)) {
        	//Fecha a janela, caso tenha sido
            glfwSetWindowShouldClose(glfwGetCurrentContext(), true);
            return;
        }
	}

	@Override
	public void draw() {
		//Solicita a limpeza da tela
		glClear(GL_COLOR_BUFFER_BIT);

		//Precisamos dizer qual VAO iremos desenhar
		glBindVertexArray(vao);

		//E qual shader program irá ser usado durante o desenho
		glUseProgram(shader);

		//Agora que todas as variáveis dos shaders estão associadas, comandamos o desenho
		//Devemos informar que a nossa malha contém triângulos. Iremos começar pelo primeiro vértice e desenhar
		//3 vértices. Observe que uma malha maior (por exemplo, de um quadrado) poderia conter 6 vértices.
		//Nesse caso, teríamos um array com 6 posições (2 triangulos) definido no init.
		glDrawArrays(GL_TRIANGLES, 0, 3);

		glBindVertexArray(0);
		glUseProgram(0);
	}

	@Override
	public void deinit() {
	}

	public static void main(String[] args) {
		new Window(new Triangle()).show();
	}
}
