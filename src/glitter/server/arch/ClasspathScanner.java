package glitter.server.arch;

import static ox.util.Utils.propagate;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.List;
import com.google.common.collect.Lists;

public class ClasspathScanner {

  @SuppressWarnings("unchecked")
  public static <T> List<Class<? extends T>> findSubclasses(Class<T> root) {
    List<Class<? extends T>> ret = Lists.newArrayList();
    File dir = new File(root.getResource(".").getFile());
    try {
      Files.walk(dir.toPath()).forEach(path -> {
        String s = path.toString();
        if (s.endsWith(".class") && !s.contains("$")) {
          try {
            s = s.replace('/', '.');
            s = s.substring(s.indexOf("glitter"), s.length() - 6);
            Class<?> c = ClassLoader.getSystemClassLoader().loadClass(s);
            if (!Modifier.isAbstract(c.getModifiers())) {
              if (root.isAssignableFrom(c)) {
                ret.add((Class<? extends T>) c);
              }
            }
          } catch (Exception e) {
            throw propagate(e);
          }
        }
      });
    } catch (IOException e) {
      throw propagate(e);
    }

    return ret;
  }

}
