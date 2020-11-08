import sun.misc.Unsafe;

public class LongLongMap {

    /**
     * Размер одной порции данных(значения) в байтах.
     * Используется для определения смещения.
     */
    public final static int VARIABLE_SIZE = Long.SIZE / Byte.SIZE;

    private final long initialAddress;
    private final long size;
    private final Unsafe unsafe;

    /**
     * Максимальное смещение в памяти, куда может быть помещён объект
     */
    private final long finalOffset;

    /**
     * @param unsafe  для доступа к памяти
     * @param address адрес начала выделенной области памяти
     * @param size    размер выделенной области в байтах (~100GB)
     */
    public LongLongMap(Unsafe unsafe, long address, long size) {
        this.unsafe = unsafe;
        this.initialAddress = address;
        this.size = size;

        this.finalOffset = this.initialAddress + this.size - VARIABLE_SIZE;

        // Инициализируем память нулями. При 100Гб может быть долго, однако гарантированно защитит от мусора в значениях.
        unsafe.setMemory(this.initialAddress, this.size, (byte) 0);
    }

    /**
     * Метод должен работать со сложностью O(1) при отсутствии коллизий, но может деградировать при их появлении
     *
     * @param k произвольный ключ
     * @param v произвольное значение
     * @return предыдущее значение или 0
     */
    long put(long k, long v) {

        long entityOffset = this.getOffsetByKey(k);

        // Если смещение текущего элемента выходит за границы выделеной области - объект не влезет. Бросаем ислючение.
        // В данной реализации в памяти неизбежно будут возникать "дыры".
        if (entityOffset > this.finalOffset)
            throw new ArrayIndexOutOfBoundsException("Индекс находится за пределами выделенной памяти");

        long oldValue = this.get(k);
        unsafe.putLong(entityOffset, v);

        return oldValue;
    }

    /**
     * Метод должен работать со сложностью O(1) при отсутствии коллизий, но может деградировать при их появлении
     *
     * @param k ключ
     * @return значение или 0
     */
    long get(long k) {
        return unsafe.getLong(this.getOffsetByKey(k));
    }

    /**
     * Метод возвращает адрес нужного значения с учётом смещения
     * @param k ключ
     * @return значение адреса с учётом смещения.
     */
    private long getOffsetByKey(long k) {
        return this.initialAddress + VARIABLE_SIZE * k;
    }
}
