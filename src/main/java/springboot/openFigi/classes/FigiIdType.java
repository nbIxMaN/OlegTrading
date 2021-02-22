package springboot.openFigi.classes;

public enum FigiIdType {
    /**
     * ISIN - International Securities Identification Number.
     */
    ID_ISIN,
    /**
     * Unique Bloomberg Identifier - A legacy, internal Bloomberg identifier.
     */
    ID_BB_UNIQUE,
    /**
     * Sedol Number - Stock Exchange Daily Official List.
     */
    ID_SEDOL,
    /**
     * Common Code - A nine digit identification number.
     */
    ID_COMMON,
    /**
     * Wertpapierkennnummer/WKN - German securities identification code.
     */
    ID_WERTPAPIER,
    /**
     * CUSIP - Committee on Uniform Securities Identification Procedures.
     */
    ID_CUSIP,
    /**
     * CINS - CUSIP International Numbering System.
     */
    ID_CINS,
    /**
     * A legacy Bloomberg identifier.
     */
    ID_BB,
    /**
     * A legacy Bloomberg identifier (8 characters only).
     */
    ID_BB_8_CHR,
    /**
     * Trace eligible bond identifier issued by FINRA.
     */
    ID_TRACE,
    /**
     * Italian Identifier Number - The Italian Identification number consisting of five or six digits.
     */
    ID_ITALY,
    /**
     * Local Exchange Security Symbol - Local exchange security symbol.
     */
    ID_EXCH_SYMBOL,
    /**
     * Full Exchange Symbol - Contains the exchange symbol for futures, options, indices inclusive of base symbol and other security elements.
     */
    ID_FULL_EXCHANGE_SYMBOL,
    /**
     * Composite Financial Instrument Global Identifier - The Composite Financial Instrument Global Identifier (FIGI) enables users to link multiple FIGIs at the trading venue level within the same country or market in order to obtain an aggregated view for an instrument within that country or market.
     */
    COMPOSITE_ID_BB_GLOBAL,
    /**
     * Share Class Financial Instrument Global Identifier - A Share Class level Financial Instrument Global Identifier is assigned to an instrument that is traded in more than one country. This enables users to link multiple Composite FIGIs for the same instrument in order to obtain an aggregated view for that instrument across all countries (globally).
     */
    ID_BB_GLOBAL_SHARE_CLASS_LEVEL,
    /**
     * Financial Instrument Global Identifier (FIGI) - An identifier that is assigned to instruments of all asset classes and is unique to an individual instrument. Once issued, the FIGI assigned to an instrument will not change.
     */
    ID_BB_GLOBAL,
    /**
     * Security ID Number Description - Descriptor for a financial instrument. Similar to the ticker field, but will provide additional metadata data.
     */
    ID_BB_SEC_NUM_DES,
    /**
     * Ticker - Ticker is a specific identifier for a financial instrument that reflects common usage.
     */
    TICKER,
    /**
     * An indistinct identifier which may be linked to multiple instruments. May need to be combined with other values to identify a unique instrument.
     */
    BASE_TICKER,
    /**
     * CUSIP (8 Characters Only) - Committee on Uniform Securities Identification Procedures.
     */
    ID_CUSIP_8_CHR,
    /**
     * OCC Symbol - A twenty-one character option symbol standardized by the Options Clearing Corporation (OCC) to identify a U.S. option.
     */
    OCC_SYMBOL,
    /**
     * Unique Identifier for Future Option - Bloomberg unique ticker with logic for index, currency, single stock futures, commodities and commodity options.
     */
    UNIQUE_ID_FUT_OPT,
    /**
     * OPRA Symbol - Option symbol standardized by the Options Price Reporting Authority (OPRA) to identify a U.S. option.
     */
    OPRA_SYMBOL,
    /**
     * Trading System Identifier - Unique identifier for the instrument as used on the source trading system.
     */
    TRADING_SYSTEM_IDENTIFIER,
    /**
     * An exchange venue specific code to identify fixed income instruments primarily traded in Asia.
     */
    ID_SHORT_CODE,
    /**
     * Index code assigned by the index provider for the purpose of identifying the security.
     */
    VENDOR_INDEX_CODE
}
