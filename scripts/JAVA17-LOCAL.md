# Java 17 Local - Guia de Instalação

## 📦 Java 17 Embutido na Aplicação

O Java 17 será instalado diretamente na pasta do projeto, tornando a aplicação **100% portátil**.

## 🎯 Estrutura Final

```
C:\Desenvolvimento\IntegradorHub\
├── java\
│   └── jdk-17\              # Java 17 embutido (~160MB)
│       ├── bin\
│       │   ├── java.exe
│       │   └── ...
│       ├── lib\
│       └── ...
├── scripts\
│   ├── install-java17.bat   # Instalação automática
│   └── start-with-java.bat  # Execução com Java local
├── target\
│   └── IntegradorHub-1.0.jar
└── ... (outros arquivos)
```

## 🚀 Instalação Automática (Recomendado)

### 1) Executar script de instalação
```cmd
cd C:\Desenvolvimento\IntegradorHub
scripts\install-java17.bat
```

### 2) O que o script faz:
- ✅ Baixa Java 17 automaticamente
- ✅ Extrai para `java\jdk-17\`
- ✅ Testa a instalação
- ✅ Limpa arquivos temporários

### 3) Resultado esperado:
```
SUCESSO: Java 17 instalado em C:\Desenvolvimento\IntegradorHub\java\jdk-17
openjdk version "17.0.x" 202x-xx-xx
OpenJDK Runtime Environment (build 17.0.x+xx)
OpenJDK 64-Bit Server VM (build 17.0.x+xx, mixed mode, sharing)
```

## 🔧 Instalação Manual

Se preferir instalação manual:

### 1) Baixar Java 17
- **URL**: https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip
- **Salvar em**: `C:\Temp\JavaDownloads\`

### 2) Extrair manualmente
```cmd
# Criar pasta
mkdir C:\Desenvolvimento\IntegradorHub\java

# Extrair o ZIP para C:\Desenvolvimento\IntegradorHub\java\
# Resultado: C:\Desenvolvimento\IntegradorHub\java\jdk-17.0.2\

# Renomear para padronizar
ren "C:\Desenvolvimento\IntegradorHub\java\jdk-17.0.2" "jdk-17"
```

### 3) Validar instalação
```cmd
C:\Desenvolvimento\IntegradorHub\java\jdk-17\bin\java.exe -version
```

## 🎮 Como Usar

### Compilar e Executar
```cmd
# 1) Compilar com Java 17
mvn clean package

# 2) Executar com Java 17 local
scripts\start-with-java.bat 17

# 3) Executar com Java 8 (se existir globalmente)
scripts\start-with-java.bat 8
```

### Prioridade de Detecção
O script busca Java nesta ordem:
1. **Local**: `java\jdk-17\bin\java.exe` ⭐ (prioridade)
2. **Sistema**: `C:\Program Files\Java\jdk-17\`
3. **x86**: `C:\Program Files (x86)\Java\jdk-17\`
4. **Custom**: `C:\Java\jdk-17\`

## ✅ Benefícios

### 🚀 **Portabilidade Total**
- Zero dependências externas
- Funciona em qualquer máquina Windows
- Copie a pasta e execute

### 🔄 **Side-by-Side**
- Java 17 local + Java 8 global
- Sem conflitos
- Teste ambas versões

### 🛡️ **Isolamento**
- Não afeta outras aplicações
- Sem modificações no PATH global
- Sem registro no Windows

### 📦 **Distribuição**
- Pacote único com tudo embutido
- Facilita deploy em clientes
- Versão controlada do Java

## 🗂️ Tamanho e Espaço

- **Download**: ~160MB
- **Extraído**: ~300MB
- **Espaço total**: ~300MB na pasta do projeto

## 🔍 Validação

### Verificar estrutura
```cmd
dir C:\Desenvolvimento\IntegradorHub\java\jdk-17\bin
```

Deve mostrar:
```
java.exe
javac.exe
javadoc.exe
... (outros executáveis)
```

### Testar execução
```cmd
scripts\start-with-java.bat 17
```

Deve iniciar a aplicação com Java 17.

## 🚨 Solução de Problemas

### "Java não encontrado"
- Verifique se `java\jdk-17\bin\java.exe` existe
- Execute `scripts\install-java17.bat` novamente

### "Erro de permissão"
- Execute como Administrador
- Verifique se o antivirus não bloqueou

### "Espaço insuficiente"
- Libere ~500MB no disco
- Limpe `C:\Temp\JavaDownloads` se necessário

## 🎯 Próximos Passos

1. **Instalar**: `scripts\install-java17.bat`
2. **Compilar**: `mvn clean package`
3. **Testar**: `scripts\start-with-java.bat 17`
4. **Validar**: Verificar logs e funcionamento

O Java 17 estará completamente embutido e pronto para uso!
