package fixtures;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.AbstractSyntaxTree;

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

    public static List<JavaFile> createJavaFilesFactory() {
        return List.of(
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
                        .build());
    }

    public static List<JavaFile> createJavaFilesStrategy() {
        return List.of(
                JavaFile.builder()
                        .name("MovieTicket.java")
                        .path("stratety/")
                        .originalClass(STRATEGY_CLAZZ)
                        .parsed(AbstractSyntaxTree.parseSingle(STRATEGY_CLAZZ))
                        .build());
    }
}
