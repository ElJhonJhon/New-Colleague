# New Colleague Mod (NeoForge 26.2 & 26.3)

Mod de entidad viva animada con **GeckoLib** para Minecraft NeoForge 26.2 y 26.3.
Entidad: **Kobold Girl** (`kobold_girl`).

---

## 📁 Dónde exportar desde Blockbench

Cuando exportes desde **Blockbench**, guarda los archivos en estas rutas exactas dentro del proyecto:

| Recurso | Menú en Blockbench | Ruta de destino en el mod |
| :--- | :--- | :--- |
| **Modelo 3D** | `Archivo > Exportar > Exportar modelo GeckoLib` | `src/main/resources/assets/newcolleaguemod/geckolib/models/entity/kobold_girl.geo.json` |
| **Animaciones** | `Archivo > Exportar > Exportar animaciones GeckoLib` | `src/main/resources/assets/newcolleaguemod/geckolib/animations/entity/kobold_girl.animation.json` |
| **Textura** | Guardar textura como PNG | `src/main/resources/assets/newcolleaguemod/textures/entity/kobold_girl.png` |

> 💡 **Nota sobre las animaciones en Blockbench:**  
> Nombra tus animaciones en Blockbench como:
> - `idle` (reposo)
> - `walk` (caminar)
> El código las enlazará automáticamente con el movimiento del mob.

---

## 🚀 Probar y Compilar

* **Para probar el mod en Minecraft:**
  Haz doble clic en:
  👉 **`Iniciar_Mod.bat`**

* **Para compilar el archivo `.jar` final:**
  Haz doble clic en:
  👉 **`Compilar_JAR.bat`** (se abrirá la carpeta `build/libs/` con el `.jar` listo para usar).
