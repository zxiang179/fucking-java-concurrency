package com.oldratlee.fucking.concurrency;

/**
 * @author Jerry Lee(oldratlee at gmail dot com)
 */
public class InvalidLongDemo {
    long count = 0;

    public static void main(String[] args) {
        // LoadMaker.makeLoad();

        InvalidLongDemo demo = new InvalidLongDemo();

        Thread thread = new Thread(demo.getConcurrencyCheckTask());
        thread.start();

        for (int i = 0; ; i++) {
            long l = i;
            // 高四字节和低四字节(32位)都是相同的
            demo.count = l << 32 | l;
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        public void run() {
            int c = 0;
            for (int i = 0; ; i++) {
                long l = count;
                long high = l >>> 32; // 高四个字节 无符号有移，高位补0
                long low = l & 0xFFFFFFFFL; // 低四个字节，截取后面的32位
                /**
                 * ByteBuffer buf = ByteBuffer.allocate(32767);
                 * buf.putLong(0,7477447194653012086L);
                 * System.out.println("1 getLong = "+(buf.getLong(0)));                    // A
                 * System.out.println("2 getLong = "+(buf.getLong(0)&0xffffffff));         // B
                 * System.out.println("3 getLong = "+(buf.getLong(0)&0xffffffffL));        // C
                 *
                 * 1 getLong = 7477447194653012086
                 * 2 getLong = 7477447194653012086
                 * 3 getLong = 940093558
                 *
                 * 我多加了A。
                 * 对于BC之间的区别，我们单独看 后半部分。
                 *
                 * 0xffffffff    // 对于数值类型，java中默认为int型，而且凑巧这个字段有是32个1，即int型中的-1，所以被解释成-1。（java中以补码的方式进行值的运算的，32个1的int型补码是-1）
                 * 0xffffffffL   // long型，没达到最大位数。二进制为32个1，值就为2的32次方。（因为没到最大位，32个1的long型补码就是2的32次方）
                 */
                if (high != low) {
                    // 如果long的高四位和第四位不同，则表示long的写入出现了线程安全问题
                    c++;
                    System.err.printf("Fuck! Got invalid long!! check time=%s, happen time=%s(%s%%), count value=%s|%s\n",
                            i + 1, c, (float) c / (i + 1) * 100, high, low);
                } else {
                    // 如果去掉这个输出，则在我的开发机上没有观察到invalid long
                    System.out.printf("Emm... %s|%s\n", high, low);
                }
            }
        }
    }

}
