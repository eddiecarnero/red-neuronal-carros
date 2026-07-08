# IA Conducción Autónoma - Neuroevolución (Red Neuronal + Algoritmo Genético)

Este proyecto es una aplicación interactiva desarrollada **desde cero en Java Puro (Swing)**. Implementa una simulación en la que una población de 100 carros inteligentes aprende de forma autónoma a conducir a lo largo de un circuito cerrado (pista de carreras) sin colisionar con los bordes (paredes internas o externas).

Para decidir los movimientos de dirección y velocidad, cada vehículo está controlado por su propia **Red Neuronal Artificial (MLP)**. Los cerebros de los vehículos son optimizados a lo largo de las generaciones mediante un **Algoritmo Genético (AG)** (neuroevolución).

Este proyecto está especialmente diseñado para cursos de **Análisis de Algoritmos (AyDA)**, ya que permite **modificar de manera dinámica el número de sensores (entradas) del cerebro en la interfaz gráfica** y reiniciar la evolución para medir experimentalmente la complejidad temporal de la red neuronal.

---

## Características de la Aplicación

1. **Circuito de Carreras Orgánico y Wavy (450x450)**:
   * La pista se genera de forma matemática aplicando una onda senoidal al radio de un círculo: $r(\theta) = R \pm 25 \sin(3\theta)$. Esto crea una pista sinuosa de ancho constante (80 píxeles) con curvas suaves y cerradas.
   * La pista cuenta con **16 checkpoints radiales** numerados del 0 al 15. Los vehículos deben cruzarlos de forma estrictamente secuencial para ganar puntos, evitando que "hagan trampa" girando en círculos en el mismo sitio o conduciendo en dirección contraria.

2. **Sensores de Distancia Dinámicos (LIDAR)**:
   * Los vehículos emiten rayos de visión delanteros en forma de arco (raycasts). Miden la distancia a las paredes internas y externas.
   * El usuario puede **modificar la cantidad de sensores ($S$) de 1 a 20 directamente desde la UI**. Al hacer clic en "Reconfigurar", la red neuronal se reconstruye con la nueva cantidad de entradas y la simulación se reinicia a la Generación 1.
   * El vehículo con mejor aptitud dibuja sus rayos de visión en vivo. Las líneas cambian a **rojo brillante** si el carro se aproxima peligrosamente a un muro.

3. **Control Completo de Conducción**:
   * A diferencia de Snake o Flappy Bird, el vehículo tiene control total sobre su física. La red neuronal genera **2 salidas continuas**:
     1. **Dirección (Steering)**: Controla el ángulo de giro (Izquierda / Derecha / Recto).
     2. **Velocidad (Acceleration)**: Controla la aceleración, el frenado y la marcha atrás.

4. **Visualización Avanzada**:
   * **Ver Todos**: Dibuja las 100 carrocerías en verde translúcido para apreciar la distribución estadística del enjambre.
   * **Ver Mejor**: Muestra únicamente al carro líder (azul brillante con parabrisas, neumáticos y faros amarillos) esquivando las curvas.
   * **Modo Manual**: Permite que el usuario conduzca usando las teclas **W/A/S/D** o las **Flechas de Dirección**. Su color es morado.

5. **Entrenamiento de Fondo Acelerado**:
   * Botones rápidos para evolucionar **10, 15 o 25 generaciones** de golpe en segundo plano usando hilos asíncronos (`SwingWorker`).

6. **Editor de Pistas Interactivo e Integrado**:
   * Permite crear circuitos personalizados directamente desde la interfaz gráfica.
   * **Modelado en Vivo**: Haz clic izquierdo y arrastra los puntos de control naranjas (waypoints) para rediseñar las curvas. El asfalto, las paredes y los 20 checkpoints se regeneran en tiempo real usando splines de Catmull-Rom.
   * **Insertar Puntos**: Doble clic en una zona libre para añadir un nuevo punto de control.
   * **Eliminar Puntos**: Clic derecho sobre un punto para borrarlo (mínimo de 4 puntos de control para formar la curva).
   * **Persistencia**: Al guardar, las coordenadas se exportan a `custom_track.txt` en la raíz del proyecto para que la pista persista entre reinicios de la aplicación y la IA comience a entrenar inmediatamente.

---

## Arquitectura de la Red Neuronal

Cada carro cuenta con un Perceptrón Multicapa (MLP) cuyas dimensiones de entrada son variables:

* **Capa de Entrada ($S + 1$ neuronas)**:
  * $S$ entradas: Distancias normalizadas $[0.0..1.0]$ medidas por los sensores de rayos (donde $S$ es elegido por el usuario de 1 a 20).
  * $1$ entrada: La velocidad lineal actual del carro normalizada.
* **Capa Oculta (10 neuronas)**:
  * Activación Tangente Hiperbólica (`tanh`).
* **Capa de Salida (2 neuronas)**:
  * Neurona 1 (Giro): Salida continua `tanh` en el rango $[-1.0, 1.0]$. Negativo gira a la izquierda, positivo gira a la derecha.
  * Neurona 2 (Acelerador): Salida continua `tanh` en el rango $[-1.0, 1.0]$. Negativo frena/retrocede, positivo acelera.

---

## Análisis de Complejidad Algorítmica $O(n)$ para tu Informe

Para tu informe de AyDA, la variable del tamaño del problema $n$ se analiza desde dos enfoques dinámicos: el número de sensores $S$ (entradas del cerebro) y el tamaño de la población $N$.

### 1. Inferencia por Frame (Red Neuronal)
Sea $S$ el número de sensores de entrada del vehículo. Con una arquitectura de capas $S+1 \to 10 \to 2$:
* Multiplicación de la primera capa (Capa de entrada a oculta):
  $$\text{Operaciones} = (S + 1) \text{ entradas} \times 10 \text{ neuronas ocultas} = O(S)$$
* Multiplicación de la segunda capa (Capa oculta a salida):
  $$\text{Operaciones} = 10 \text{ neuronas ocultas} \times 2 \text{ salidas} = O(1) \quad (\text{constante})$$
* **Complejidad Temporal de un paso de inferencia**:
  $$T_{\text{inferencia}}(S) = O(S + 1) \cdot 10 + 10 \cdot 2 \implies \mathbf{O(S)}$$
  *Justificación*: Cambiar el número de sensores del vehículo en la interfaz gráfica escala la complejidad computacional del "pensamiento" de forma **estrictamente lineal $O(S)$** respecto al número de sensores.

### 2. Simulación por Frame (Toda la población)
Si tenemos una población de $N$ carros, cada uno con $S$ sensores:
* El cálculo de raycasts (intersección de rayos contra paredes) requiere evaluar $S$ rayos contra $W$ segmentos de muros.
* **Complejidad por Frame**:
  $$T_{\text{frame}}(N, S) = N \times (O(S \cdot W)_{\text{raycast}} + O(S)_{\text{red\_neuronal}}) \implies \mathbf{O(N \cdot S)}$$

### 3. Paso de Evolución
Ocurre una vez al final de cada generación cuando mueren todos los vehículos.
* Ordenar la población por Timsort: $O(N \log N)$.
* Crossover y mutación del genoma de tamaño $G = (S + 1) \cdot 10 + 10 \cdot 2 + 12$ para toda la población: $O(N \cdot S)$.
* **Complejidad de la Evolución**:
  $$T_{\text{evolución}}(N, S) = \mathbf{O(N \log N + N \cdot S)}$$

---

## Cómo Compilar y Ejecutar

### Método de Doble Clic en Windows
1. Entra a la carpeta `red-neuronal-carro`.
2. Ejecuta el archivo **`run.bat`** (o `.\run.ps1` en PowerShell).
3. El código fuente se compilará con `javac` y se ejecutará automáticamente con `java`.

### Compilación Manual en Terminal
1. Abre tu terminal en `red-neuronal-carro`.
2. Compila el código:
   ```bash
   javac -encoding UTF-8 -d target/classes src/com/proyecto/carro/Main.java src/com/proyecto/carro/model/*.java src/com/proyecto/carro/gui/*.java
   ```
3. Ejecuta la aplicación:
   ```bash
   java -cp target/classes com.proyecto.carro.Main
   ```
