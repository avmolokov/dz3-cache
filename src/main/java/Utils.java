import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

    public static <T> T cache(T obj) {
        InvocationHandler handler = new CacheHandler(obj);
        @SuppressWarnings("unchecked")
        T cachedObj = (T) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                handler
        );
        return cachedObj;
    }

    private static class CacheHandler implements InvocationHandler {
        private final Object obj;
        private Map<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();
        private  int setCount = 2; // кол-во вызоывов вставки изменений
        private static final int MAXSET = 3; // после этого запускаем чистку

        public CacheHandler(Object obj) {
            this.obj = obj;
        }

        private void cleanUpCache() {
            System.out.println(" чистим не актульные");
                cache.entrySet().removeIf(entry -> entry.getValue().notActual());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (setCount >= MAXSET) {
                setCount = 0;
                 new Thread(()->cleanUpCache()).start();
            }

            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(method.getName());
            keyBuilder.append(".");
            keyBuilder.append(Arrays.toString(args));
            keyBuilder.append(".");
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                keyBuilder.append("["+field.getInt(obj)+']');
            }

            CacheKey key = new CacheKey(keyBuilder.toString(), method);
            CacheEntry entry = cache.get(key);

            Method frMethod = obj.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());

            if (frMethod.isAnnotationPresent(Cache.class)) {
                var lTime = frMethod.getAnnotation(Cache.class).value();
                if (entry == null || !entry.isActual()) {
                    setCount++;
                    System.out.println("попали в  Cache на новый расчет = ");
                    Object result = method.invoke(obj, args);
                    entry = new CacheEntry(result,lTime);
                    cache.put(key, entry);
                } else {
                    System.out.println("попали в  Cache на обновения времени  ");
                    entry.refreshExpiration(lTime);
                }
                return entry.getResult();
            }


            return method.invoke(obj, args);
        }

        private static class CacheKey {
            String key;
            Method method;

            public CacheKey(String key, Method method) {
                this.key = key;
                this.method = method;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CacheKey cacheKey = (CacheKey) o;
                return Objects.equals(key, cacheKey.key) && Objects.equals(method, cacheKey.method);
            }

            @Override
            public int hashCode() {
                return Objects.hash(key, method);
            }
        }


        private static class CacheEntry {
            Object result;
            Long expirationTime;

            public CacheEntry(Object result, Integer Ltime) {
                this.result = result;
                if (Ltime != null) {
                    this.expirationTime = System.currentTimeMillis() + Ltime;
                }
            }
            public Object getResult() {
                return result;
            }

            public boolean isActual() {
                return (expirationTime == null || this.expirationTime >=  System.currentTimeMillis());
            }
            public boolean notActual() {
                return !(expirationTime == null || this.expirationTime >=  System.currentTimeMillis());
            }

            public void refreshExpiration(Integer Ltime) {
                if (Ltime != null) {
                    this.expirationTime = System.currentTimeMillis() + Ltime;
                }
            }

            @Override
            public String toString() {
                return "CacheEntry{" +
                        "result=" + result +
                        ", expirationTime=" + expirationTime +
                        '}';
            }
        }

    }

}



