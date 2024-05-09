package fixtures;

import br.com.detection.detectionagent.domain.dataExtractions.ast.AstHandler;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014FactoryCandidate;
import br.com.detection.detectionagent.domain.methods.weiL.WeiEtAl2014StrategyCandidate;
import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.AbstractSyntaxTree;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Wei {

    private static final String FACTORY_INTERFACE = """
            package factory;

            public interface Logger{
              void writeLog();
            }
                       """;
    private static final String FACTORY_DATABASE_LOGGER_IMPLEMENTATION = """
            package factory;

            public class DataBaseLogger implements Logger {
              
              public void writeLog(String message) {
                System.out.println("Logging to database: " + message);
              }
            }
            """;
    private static final String FACTORY_LOGGER_IMPLEMENTATION = """
            package factory;

            public class FileLogger implements Logger {
              
              public void writeLog(String message) {
                System.out.println("Logging to file: " + message);
              }
            }
            """;

    private static final String FACTORY = """
            package factory;

            public class LoggerFactory {
              
              public Logger createLogger(char type){
                if(type == 'D'){
                  Logger logger = new DataBaseLogger();
                  return logger;
                }
                else if(type == 'F'){
                  Logger logger = new FileLogger();
                  return logger;
                }
                else{
                  return null;
                }

              }
            }
            """;

    private static final String STRATEGY_CLAZZ = """
            package stratety;

            class MovieTicket {

              private Double price; 

              public Double calculate(char type){

                if(type == 'S'){
                    return price * 0.8;

                }

                if(type == 'C'){
                    return price - 10;
                }

                else if (type == 'M'){
                    return price * 0.5;
                }

                else {
                    return -1;
                }
              }

              public void setPrice(Double price) {
                this.price = price;
              }

            }
            """;
    public static final String LOGGER_FACTORY_REFACTORED = """
package factory;

public abstract class LoggerFactory {

    public abstract Logger createLogger();
}
""";

    public static final String DATA_BASE_LOGGER_FACTORY_REFACTORED = """
package factory;

public class DataBaseLoggerFactory extends LoggerFactory {

    public Logger createLogger() {
        Logger logger = new DataBaseLogger();
        return logger;
    }
}
""";

    public static final String FILE_LOGGER_FACTORY_REFACTORED = """
package factory;

public class FileLoggerFactory extends LoggerFactory {

    public Logger createLogger() {
        Logger logger = new FileLogger();
        return logger;
    }
}
""";

    public static final String MOVE_TICKET__REFACTORED = """
package stratety;

class MovieTicket {

    private Double price;

    public Double calculate(Strategy strategy) {
        return strategy.calculate();
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
""";

    public static final String STRATEGY_REFACTORED = """
public abstract class Strategy {

    public abstract Double calculate() {
    }
}
""";

    public static final String STRATEGY_S_REFACTORED = """
public class ConcreteStrategyS extends Strategy {

    public Double calculate() {
        return price * 0.8;
    }
}
""";

    public static final String STRATEGY_C_REFACTORED = """
public class ConcreteStrategyC extends Strategy {

    public Double calculate() {
        return price - 10;
    }
}
""";

    public static final String STRATEGY_M_REFACTORED = """
public class ConcreteStrategyM extends Strategy {

    public Double calculate() {
        return price * 0.5;
    }
}
""";

    public static List<JavaFile> createJavaFilesFactory() {
        return new ArrayList<>(Arrays.asList(
                JavaFile.builder()
                        .name("Logger.java")
                        .path("factory/")
                        .originalClass(FACTORY_INTERFACE)
                        .parsed(AbstractSyntaxTree.parseSingle(FACTORY_INTERFACE))
                        .build(),
                JavaFile.builder()
                        .name("FileLogger.java")
                        .path("factory/")
                        .originalClass(FACTORY_LOGGER_IMPLEMENTATION)
                        .parsed(AbstractSyntaxTree.parseSingle(FACTORY_LOGGER_IMPLEMENTATION))
                        .build(),
                JavaFile.builder()
                        .name("DataBaseLogger.java")
                        .path("factory/")
                        .originalClass(FACTORY_DATABASE_LOGGER_IMPLEMENTATION)
                        .parsed(AbstractSyntaxTree.parseSingle(FACTORY_DATABASE_LOGGER_IMPLEMENTATION))
                        .build(),
                JavaFile.builder()
                        .name("LoggerFactory.java")
                        .path("factory/")
                        .originalClass(FACTORY)
                        .parsed(AbstractSyntaxTree.parseSingle(FACTORY))
                        .build()));
    }

    public static RefactoringCandidate createFactoryCandidate() {
        var file = JavaFile.builder()
                .name("LoggerFactory.java")
                .path("factory/")
                .originalClass(FACTORY)
                .parsed(AbstractSyntaxTree.parseSingle(FACTORY))
                .build();
        var method = AstHandler.getMethods(file.getCompilationUnit()).getFirst();

        return WeiEtAl2014FactoryCandidate.builder()
                .file(file)
                .compilationUnit(file.getCompilationUnit())
                .packageDcl(file.getCompilationUnit().getPackageDeclaration().orElse(null))
                .classDcl(AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit()).orElse(null))
                .methodDcl(method)
                .ifStatements(AstHandler.getIfStatements(method).stream().toList())
                .build();
    }

    public static List<JavaFile> createJavaFilesStrategy() {
        return new ArrayList<>(Collections.singletonList(
                JavaFile.builder()
                        .name("MovieTicket.java")
                        .path("stratety/")
                        .originalClass(STRATEGY_CLAZZ)
                        .parsed(AbstractSyntaxTree.parseSingle(STRATEGY_CLAZZ))
                        .build()));
    }

    public static RefactoringCandidate createStrategyCandidate() {
        var file = JavaFile.builder()
                .name("MovieTicket.java")
                .path("stratety/")
                .originalClass(STRATEGY_CLAZZ)
                .parsed(AbstractSyntaxTree.parseSingle(STRATEGY_CLAZZ))
                .build();
        var method = AstHandler.getMethods(file.getCompilationUnit()).getFirst();

        return WeiEtAl2014StrategyCandidate.builder()
                .file(file)
                .compilationUnit(file.getCompilationUnit())
                .packageDcl(file.getCompilationUnit().getPackageDeclaration().orElse(null))
                .classDcl(AstHandler.getClassOrInterfaceDeclaration(file.getCompilationUnit()).orElse(null))
                .methodDcl(method)
                .ifStatements(AstHandler.getIfStatements(method).stream().toList())
                .variables(AstHandler.getVariableDeclarations(method).stream().toList())
                .build();
    }
}
