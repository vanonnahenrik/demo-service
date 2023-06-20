package de.dsa.prodis.service.registry.util;

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DataReadQuery;
import org.eclipse.persistence.sequencing.QuerySequence;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

class LookupEnumByName {
    public static <E extends Enum<E>> E lookup(Class<E> enumClass, String id) {
        try {
            E result = Enum.valueOf(enumClass, id);
            return result;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid value for enum " + enumClass.getSimpleName() + ": " + id);
        }
    }
}

/**
 * For each Sequence used, configure ora and pg names.
 */
class Sequences {

    private final static Logger LOG = LoggerFactory.getLogger(Sequences.class);

    public final static String ORA_NEXTVAL_FORMAT = "SELECT %s.NEXTVAL FROM DUAL";
    public final static String PG_NEXTVAL_FORMAT = "select nextval('%s'::regclass)";

    final String oraName;
    final String pgName;
    final String oraSelect;
    final String pgSelect;

    public Sequences(String oraName, String pgName) {
        this.oraName = oraName;
        this.pgName = pgName;
        oraSelect = String.format(ORA_NEXTVAL_FORMAT, oraName);
        pgSelect = String.format(PG_NEXTVAL_FORMAT, pgName);
        LOG.debug("Oracle select from sequence: {}", oraName);
        LOG.debug("Postgres select from sequence: {}", pgName);
    }
    public String getOraName() {
        return oraName;
    }
    public String getPgName() {
        return pgName;
    }
    public String getOraSelect() {
        return oraSelect;
    }
    public String getPgSelect() {
        return pgSelect;
    }
}

/**
 * Configure all sequences you need to use as generators. The generator needs to know
 * the names for both oracle and postgres sequences.
 *
 *  In the JPA-annotations of the entity, you use the generator name:
 *
 *  @GeneratedValue(generator = "STAT_PARAMETER_SEQ")
 *
 *  It will then take the id from the generator code and hopefully from the correct database
 *  sequence.
 *
 */
enum Generator {
    STAT_PARAMETER_SEQ(new Sequences("SEQ_STAT_PARAMETER", "stat_parameter_reportid_seq")),
    PRIVILEGE_OBJECT_SEQ(new Sequences("SEQ_PRIVILEGE_OBJECT", "privilege_object_objectid_seq"))
    ;

    private final Sequences sequenceNames;
    private Generator(Sequences sequenceNames) {
        this.sequenceNames = sequenceNames;
    }
    public Sequences getSequenceNames() {
        return sequenceNames;
    }
}

@Component
@ConfigurationProperties("spring.datasource.tomcat")
public class CustomIdSequence extends QuerySequence implements SessionCustomizer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CustomIdSequence.class);

    public static String DB_PRODUCT_NAME = null;
    public static boolean IS_ORA = false;
    
    private static String SCHEMA_NAME = null;

    /*
     * get jdbc driver from application properties
     */
    private String url;
    
    /*
     * get schema name from application properties
     */
    private String schemaName;

    public CustomIdSequence() {
        super();
    }

    public CustomIdSequence(String name) {
        super(name);
    }

    public CustomIdSequence(String name, int size) {
        super(name, size);
    }

    public CustomIdSequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    @Override
    public void customize(Session session) throws Exception {

    	if (StringUtils.hasText(SCHEMA_NAME)) {
            session.getLogin().setTableQualifier(SCHEMA_NAME);
        }
    	
        for (String name : getNames(Generator.class)) {
            // Register Generator name as sequence
            CustomIdSequence sequence = new CustomIdSequence(name);
            LOG.info("Adding Sequence: {}", name);
            session.getLogin().addSequence(sequence);
        }

    }

    @Override
    public boolean shouldAcquireValueAfterInsert() {
        return false;
    }

    @Override
    public boolean shouldUseTransaction() {
        return false;
    }

    @Override
    public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName) {

        Generator g = LookupEnumByName.lookup(Generator.class, seqName);
        DataReadQuery query = IS_ORA ? new DataReadQuery(g.getSequenceNames().oraSelect) : new DataReadQuery(g.getSequenceNames().pgSelect);

        query.setResultType(DataReadQuery.VALUE);
        return writeSession.executeQuery(query);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Vector getGeneratedVector(Accessor accessor, AbstractSession writeSession, String seqName, int size) {
        LOG.debug("Generating pre-allocation Vector: {} {}", seqName, size);
        Vector v =  new Vector(size);
        for (int i=0; i<size; i++) {
            v.add(getGeneratedValue(accessor, writeSession, seqName));
        }
        return v;
    }

    @Override
    public void onConnect() {
    }

    @Override
    public void onDisconnect() {
    }
    
    public String getSchemaName() {
    	return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
    	this.schemaName = schemaName;
    	SCHEMA_NAME = schemaName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        LOG.info("Driver is set by Spring configuration: " + url);
        this.url = url;
        IS_ORA = url.toLowerCase().startsWith("jdbc:oracle");
        LOG.info("is oracle: {}", IS_ORA);
    }

}
