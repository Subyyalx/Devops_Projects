<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:agile="http://www.oracle.com/technology/products/applications/xml/plm/2010/09/">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="agile:RedlinedBOMRow[@RedlineAction = 'Deleted']/agile:RedlinedBOMRowPrevious/agile:Qty/text()">0</xsl:template>

</xsl:stylesheet>