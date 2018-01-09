package test.test;

import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by liangjianhua on 2017/11/30.
 */
public class TestMain {

    public static void main(String []args){
//        RewriterAgent.premain(null , null);
//        String p2=TestMain.class.getResource("../").getPath();
//        System.out.println("JdomParse.class.getResource---"+p2);
//        InputStream resource = ClassRemapperConfig.class.getClassLoader().getResourceAsStream( "/type_map.properties ");
//        System.out.println("JdomParse.class.getResource---"+resource);
//
//        File file=new File("type_map.properties");
//        try {
//            FileInputStream fileInputStream = new FileInputStream(file);
//            Properties properties = new Properties();
//            properties.load( fileInputStream );
//
//            Map<String ,String > map = (Map) properties;
//
//            Iterator<String> iterator = map.keySet().iterator();
//            while ( iterator.hasNext() ){
//                String key = iterator.next();
//
//                System.out.println("props.put(\""+ key + "\"," + "\"" + map.get(key) + "\");" );
//            }
//
//
//        }catch ( Exception e ){
//
//        }
//        System.out.println( 1 | 2 );
//        System.out.println(  ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES  );
        File file = new File("./build.gradle");
        System.out.println("file : " + file.exists() );
    }



}
