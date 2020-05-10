package com.zxh.compiler;

import com.google.auto.service.AutoService;
import com.zxh.annotation.BindView;
import com.zxh.annotation.ClickView;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class ButterknifeProcessor extends AbstractProcessor {

    //用来报告错误，警告和其他提示信息
    private Messager mMessager;
    // Elements中包含操作Element的工具类
    private Elements mElementsUtils;
    //Filer创建新的源文件 class以及辅助文件
    private Filer mFiler;
    //Types中包含操作TypMirror的工具方法
    private Types mTypeUtils;
    String  activityName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager=processingEnvironment.getMessager();
        mElementsUtils=processingEnvironment.getElementUtils();
        mFiler=processingEnvironment.getFiler();
        mTypeUtils=processingEnvironment.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // 添加支持BindView注解的类型
        Set<String> set=new LinkedHashSet<>();
        set.add(BindView.class.getCanonicalName());
        set.add(ClickView.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // 返回此注释 Processor 支持的最新的源版本，该方法可以通过注解@SupportedSourceVersion指定
        return SourceVersion.latestSupported();
    }

    /**
     * 注解处理器核心处理方法，处理具体的注解，生成java文件
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE,"=================>start");
        // 获取MainActivity中所有带BindView注解的属性
        Set<? extends Element> bindViewSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        // 获取MainActivity中所有带OnClick注解的方法
        Set<? extends Element> clickSet = roundEnvironment.getElementsAnnotatedWith(ClickView.class);
        // 保存键值对，key是com.netease.butterknife.MainActivity   value是所有带BindView注解的属性集合
        Map<String, List<VariableElement>> bindViewCache=new HashMap<>();
        // 保存键值对，key是com.netease.butterknife.MainActivity   value是所有带OnClick注解的方法集合
        Map<String, List<ExecutableElement>> clickViewCache=new HashMap<>();

        for (Element element : bindViewSet) {
            VariableElement variableElement= (VariableElement) element;
            activityName= getActivityName(variableElement);
            List<VariableElement> bindViewLists = bindViewCache.get(activityName);
            if(bindViewLists==null){
                bindViewLists=new ArrayList<>();
                bindViewCache.put(activityName,bindViewLists);
            }
            bindViewLists.add(variableElement);
        }

        for (Element element : clickSet) {
            ExecutableElement executableElement= (ExecutableElement) element;
            activityName= getActivityName(executableElement);
            List<ExecutableElement> clickViewLists = clickViewCache.get(activityName);
            if(clickViewLists==null){
                clickViewLists=new ArrayList<>();
                clickViewCache.put(activityName,clickViewLists);
            }
            clickViewLists.add(executableElement);
        }

        //生成java文件

        for(String activityName : bindViewCache.keySet()){

        // 获取"com.netease.butterknife.MainActivity"中所有控件属性的集合
        List<VariableElement> variableLists= bindViewCache.get(activityName);
        List<ExecutableElement> executableElementList = clickViewCache.get(activityName);
        try {
            // 创建一个新的源文件（Class），并返回一个对象以允许写入它
            JavaFileObject javaFileObject = mFiler.createSourceFile(activityName + "$$ViewBinder");
            String packageName=getPackageName(variableLists.get(0));
            // 定义Writer对象，开启生成代码过程
            Writer writer = javaFileObject.openWriter();
            // 类名：MainActivity$ViewBinder，不是com.netease.butterknife.MainActivity$ViewBinder
            // 通过属性元素获取它所属的MainActivity类名，再拼接后结果为：MainActivity$ViewBinder
            String activitySimpleName=variableLists.get(0).getEnclosingElement()
                    .getSimpleName().toString()+"$$ViewBinder";
            mMessager.printMessage(Diagnostic.Kind.NOTE,"=====activitySimpleName====="+activitySimpleName);

            //第一行生成包
            writer.write("package "+packageName+";\n");
            // 第二行生成要导入的接口类（必须手动导入）
            writer.write("import com.zxh.library.ViewBinder;\n");
            writer.write("import com.zxh.library.DebouncingOnClickListener;\n");
            writer.write("import android.view.View;\n");
            // 第三行生成类
            writer.write("public class " + activitySimpleName +
                    " implements ViewBinder<" + activityName + "> {\n");

            // 第四行生成bind方法
            writer.write("public void bind(final " + activityName + " target) {\n");
            // 循环生成MainActivity每个控件属性
            for (VariableElement variableElement : variableLists) {
                // 控件属性名
                String fieldName = variableElement.getSimpleName().toString();
                // 获取控件的注解
                BindView bindView = variableElement.getAnnotation(BindView.class);
                // 获取控件注解的id值
                int id = bindView.value();
                // 生成：target.tv = target.findViewById(xxx);
                writer.write("target." + fieldName + " = " + "target.findViewById(" + id + ");\n");
            }

            // 循环生成MainActivity每个点击事件
            for (ExecutableElement executableElement :executableElementList ) {
                // 获取方法名
                String methodName = executableElement.getSimpleName().toString();
                // 获取方法的注解
                ClickView onClick = executableElement.getAnnotation(ClickView.class);
                // 获取方法注解的id值
                int[] ids = onClick.value();
                for (int id : ids) {
                // 获取方法参数
                List<? extends VariableElement> parameters = executableElement.getParameters();
                // 生成点击事件
                writer.write("target.findViewById(" + id + ").setOnClickListener(new DebouncingOnClickListener() {\n");
                writer.write("public void doClick(View v) {\n");
                if (parameters.isEmpty()) {
                    writer.write("target." + methodName + "();\n}\n});\n");
                } else {
                    writer.write("target." + methodName + "(v);\n}\n});\n");
                }
            }
          }

            // 最后结束标签，造币完成
            writer.write("\n}\n}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
  }
        return false;
    }

    /**
     * 获取activity（类名）
     * @param variableElement 属性elemanet(bindview注解的属性)
     * @return
     */
    private String getActivityName(VariableElement variableElement) {
        //获取包名
       String packageName= getPackageName(variableElement);
         TypeElement typeElement= (TypeElement) variableElement.getEnclosingElement();
        String className = typeElement.getSimpleName().toString();
        mMessager.printMessage(Diagnostic.Kind.NOTE,"=====bindview==包名======"+packageName);
        mMessager.printMessage(Diagnostic.Kind.NOTE,"======bindview=类名======"+className);
          return packageName+"."+className;
    }

    /**
     * 获取activity（类名）
     * @param executableElement 属性elemanet(clickView注解的属性)
     * @return
     */
    private String getActivityName(ExecutableElement executableElement) {
        //获取包名
        String packageName= getPackageName(executableElement);
        TypeElement typeElement= (TypeElement) executableElement.getEnclosingElement();
        String className = typeElement.getSimpleName().toString();
        mMessager.printMessage(Diagnostic.Kind.NOTE,"====clickView===包名======"+packageName);
        mMessager.printMessage(Diagnostic.Kind.NOTE,"====clickView===类名======"+className);
        return packageName+"."+className;
    }
    /**
     * 获取包名
     * @param variableElement 属性elemanet(bindview注解的属性)
     * @return
     */
    private String getPackageName(VariableElement variableElement) {
        TypeElement typeElement= (TypeElement) variableElement.getEnclosingElement();
        String packageName = mElementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
        return packageName;
    }
    /**
     * 获取包名
     * @param executableElement 属性elemanet(bindview注解的属性)
     * @return
     */
    private String getPackageName(ExecutableElement executableElement) {
        TypeElement typeElement= (TypeElement) executableElement.getEnclosingElement();
        String packageName = mElementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
        return packageName;
    }
}
