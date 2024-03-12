import java.lang.reflect.*;
import java.util.*;

public class Utils {

    public static <T> T cache(T obj) {
        System.out.println("попали в кэш " + obj.getClass().getName() + " " + obj.getClass().getDeclaredMethods());
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
        private Map<CacheKey, CacheEntry> cache;

        public CacheHandler(Object obj) {
            this.obj = obj;
            this.cache = new HashMap<>();
        }

        private void cleanUpCache() {
            System.out.println(" чистим не актульные");
            cache.entrySet().removeIf(entry -> entry.getValue().notActual());
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //cleanUpCache();
            new Thread(()->cleanUpCache()).start();
            //поидеи можно оставить только hashCode(остальное до кучи наверно будет) создадим строку чтобы точно была уникально
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(obj.hashCode());
            keyBuilder.append(".");
            keyBuilder.append(obj.getClass().getName());
            keyBuilder.append(".");
            keyBuilder.append(method.getName());
            keyBuilder.append(".");
            keyBuilder.append(Arrays.toString(args));
            keyBuilder.append(".");
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                keyBuilder.append("["+field.getInt(obj)+']');
            }
            System.out.println("  keyBuilder = " + keyBuilder.toString());

            CacheKey key = new CacheKey(keyBuilder.toString(), method);
            CacheEntry entry = cache.get(key);

            Method frMethod = obj.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (entry == null) {
                System.out.println("entry  пусто");
            }
            else {
                System.out.println("entry  " + entry.toString());
            }


            if (frMethod.isAnnotationPresent(Cache.class)) {
                var lTime = frMethod.getAnnotation(Cache.class).value();
                if (entry == null || !entry.isActual()) {
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

            if (frMethod.isAnnotationPresent(Mutator.class)) {
                System.out.println("попали в  Mutator");
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



