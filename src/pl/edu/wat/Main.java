package pl.edu.wat;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ReturnStmt;
import javafx.util.Pair;

import javax.tools.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        final String fileName = "src\\Class.java";
        final String alteredFileName = "src\\ClassAltered.java";
        CompilationUnit cu;
        try (FileInputStream in = new FileInputStream(fileName)) {
            cu = JavaParser.parse(in);
        }

        functionInlining(cu);

        cu.getClassByName("Class").get().setName("ClassAltered");

        try (FileWriter output = new FileWriter(new File(alteredFileName), false)) {
            output.write(cu.toString());
        }

        File[] files = {new File(alteredFileName)};
        String[] options = {"-d", "out//production//Synthesis"};

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
            compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    Arrays.asList(options),
                    null,
                    compilationUnits).call();

            diagnostics.getDiagnostics().forEach(d -> System.out.println(d.getMessage(null)));
        }
    }


    private static void functionInlining(CompilationUnit cu) {
        //stworzenie listy<nazwa metody, zwracane wyrażenie> metod, które nie mają argumentów i zwracają "LiteralExpr"
        List<Pair<String, Expression>> methodsWithExpression = cu.getChildNodesByType(MethodDeclaration.class)
                .stream()
                .filter(m -> m.getParameters().size() == 0 && m.getChildNodesByType(ReturnStmt.class).size() == 1 &&
                        m.getChildNodesByType(ReturnStmt.class).get(0).getExpression().isPresent() &&
                        m.getChildNodesByType(ReturnStmt.class).get(0).getExpression().get().isLiteralExpr())
                .map(m -> new Pair<>(m.getNameAsString(), m.getChildNodesByType(ReturnStmt.class).get(0).getExpression().get()))
                .collect(Collectors.toList());

        //podmiana wszystkich wywołań metod, które zostały odnalezione powyżej
        List<MethodCallExpr> methodCallExprs = cu.getChildNodesByType(MethodCallExpr.class);
        for (MethodCallExpr call : methodCallExprs) {
            for (Pair<String, Expression> method : methodsWithExpression) {
                if (call.getNameAsString().equals(method.getKey())) {
                    if (method.getValue().isStringLiteralExpr())
                        call.replace(new StringLiteralExpr(method.getValue().asStringLiteralExpr().asString()));
                    else if (method.getValue().isIntegerLiteralExpr())
                        call.replace(new IntegerLiteralExpr(method.getValue().asIntegerLiteralExpr().asInt()));
                    else if (method.getValue().isBooleanLiteralExpr())
                        call.replace(new BooleanLiteralExpr(method.getValue().asBooleanLiteralExpr().getValue()));
                    else if (method.getValue().isCharLiteralExpr())
                        call.replace(new CharLiteralExpr(method.getValue().asCharLiteralExpr().asChar()));
                    else if (method.getValue().isDoubleLiteralExpr())
                        call.replace(new DoubleLiteralExpr(method.getValue().asDoubleLiteralExpr().asDouble()));
                    else if (method.getValue().isLongLiteralExpr())
                        call.replace(new LongLiteralExpr(method.getValue().asLongLiteralExpr().asLong()));
                    break;
                }
            }
        }
    }
}
