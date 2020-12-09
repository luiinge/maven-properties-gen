package propertiesgen;

public final class StaticTestProperties implements TestProperties {

    public static final String STATIC_VERSION = "1.0.0";
    public static final String DESCRIPTION = "Java Processor that creates a class with static fields valued with Maven properties";
    public static final String MALFORMED = "${malformed";
    public static final byte BYTE_INCEPTION_YEAR = Byte.parseByte("2020");
    public static final Byte BYTE_BOX_INCEPTION_YEAR = Byte.valueOf("2020");
    public static final short SHORT_INCEPTION_YEAR = Short.parseShort("2020");
    public static final Short SHORT_BOX_INCEPTION_YEAR = Short.valueOf("2020");
    public static final int INT_INCEPTION_YEAR = Integer.parseInt("2020");
    public static final Integer INT_BOX_INCEPTION_YEAR = Integer.valueOf("2020");
    public static final long LONG_INCEPTION_YEAR = Long.parseLong("2020");
    public static final Long LONG_BOX_INCEPTION_YEAR = Long.valueOf("2020");
    public static final float FLOAT_INCEPTION_YEAR = Float.parseFloat("2020");
    public static final Float FLOAT_BOX_INCEPTION_YEAR = Float.valueOf("2020");
    public static final double DOUBLE_INCEPTION_YEAR = Double.parseDouble("2020");
    public static final Double DOUBLE_BOX_INCEPTION_YEAR = Double.valueOf("2020");


    @Override
    public String staticVersion() {
        return STATIC_VERSION;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public String malformed() {
        return MALFORMED;
    }

    @Override
    public byte byteInceptionYear() {
        return BYTE_INCEPTION_YEAR;
    }

    @Override
    public Byte byteBoxInceptionYear() {
        return BYTE_BOX_INCEPTION_YEAR;
    }

    @Override
    public short shortInceptionYear() {
        return SHORT_INCEPTION_YEAR;
    }

    @Override
    public Short shortBoxInceptionYear() {
        return SHORT_BOX_INCEPTION_YEAR;
    }

    @Override
    public int intInceptionYear() {
        return INT_INCEPTION_YEAR;
    }

    @Override
    public Integer intBoxInceptionYear() {
        return INT_BOX_INCEPTION_YEAR;
    }

    @Override
    public long longInceptionYear() {
        return LONG_INCEPTION_YEAR;
    }

    @Override
    public Long longBoxInceptionYear() {
        return LONG_BOX_INCEPTION_YEAR;
    }

    @Override
    public float floatInceptionYear() {
        return FLOAT_INCEPTION_YEAR;
    }

    @Override
    public Float floatBoxInceptionYear() {
        return FLOAT_BOX_INCEPTION_YEAR;
    }

    @Override
    public double doubleInceptionYear() {
        return DOUBLE_INCEPTION_YEAR;
    }

    @Override
    public Double doubleBoxInceptionYear() {
        return DOUBLE_BOX_INCEPTION_YEAR;
    }

}
