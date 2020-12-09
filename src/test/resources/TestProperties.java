package propertiesgen;

@ProjectProperties
public interface TestProperties {

    @ProjectProperty("${project.version}")
    String staticVersion();

    @ProjectProperty("${project.description}")
    String description();

    @ProjectProperty("${malformed")
    String malformed();

    @ProjectProperty("${project.inceptionYear}")
    byte byteInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    Byte byteBoxInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    short shortInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    Short shortBoxInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    int intInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    Integer intBoxInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    long longInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    Long longBoxInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    float floatInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    Float floatBoxInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    double doubleInceptionYear();

    @ProjectProperty("${project.inceptionYear}")
    Double doubleBoxInceptionYear();

}
