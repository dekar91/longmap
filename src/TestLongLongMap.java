import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class TestLongLongMap {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InstantiationException {

        if(args == null || args.length < 2) {
            throw new IllegalArgumentException("Both size and long count arguments must be provided");
        }

        // Размер выделяемой памяти в байтах
        String sizeArg = args[0];
        long size;
        if(sizeArg != null && !sizeArg.isBlank()) {
           size = Long.parseLong(sizeArg); 
        } else {
            throw new IllegalArgumentException("Size must be specified");
        }

        // Количество значений long для записи
        String countArg = args[1];
        long longCount;
        if(countArg != null && !countArg.isBlank()) {
            longCount = Long.parseLong(countArg);
        } else {
            throw new IllegalArgumentException("Long count must be specified");
        }

        Unsafe unsafe = getUnsafe();
        long startAddress = unsafe.allocateMemory(size);

        LongLongMap map = new LongLongMap(unsafe, startAddress, size);

        // Тест записи значений.
        for (long i =  0; i <=longCount; i++){
            long oldValue = map.put(i, i);

            // По-умолчанию должен возвращаться 0.
            assert oldValue == 0;
            System.out.println(oldValue);
        };

        // Тест чтения
        for(long i =  0; i <=longCount; i++) {
            long value =  map.get(i);
            assert value == i : "Given " + value +  " but expected " + i;
            System.out.println(i + "    " + value);
        };

        unsafe.freeMemory(startAddress);
    }

    private static Unsafe getUnsafe() throws IllegalAccessException, NoSuchFieldException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }
}
