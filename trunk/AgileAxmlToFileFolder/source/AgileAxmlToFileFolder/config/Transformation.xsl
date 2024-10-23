<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="
http://www.w3.org/2005/xpath-functions" xpath-default-namespace="http://www.oracle.com/technology/products/applications/xml/plm/2010/09/">
                                          

	<xsl:output method="text" encoding="UTF-8" indent="no"/>

	<xsl:variable name="Header" select=
"'Company,PartNum,PartDescription,TypeCode,IUM,LifeCycle,TrackSerialNum,SNFormat,RestrictShipTo,RestrictExcludeFrom,PartRev,RevDescription,EffectiveDate,ECONum,ECODescription,BOMLevel,BOMItemNum,BOMDescription,BOMRev,BOMQty,BOMFindNum,BOMUOM'"/>
	
	<xsl:variable name="LINE_SEPARATOR" select="'&#xD;&#xA;'"/>
	<xsl:variable name="VALUE_SEPARATOR" select="','"/>
	<xsl:variable name="NULL_VALUE" select="'##'"/>
	<xsl:variable name="CHANGE_TYPE" select="/AgileData/ChangeOrders/CoverPage/ChangeType | /AgileData/ManufacturerOrders/CoverPage/ChangeType"/>

	<xsl:template match="/">
		<xsl:value-of select="$Header"/>
		<xsl:value-of select="$LINE_SEPARATOR"/>
		<xsl:apply-templates select="/AgileData/ChangeOrders"/>
		<xsl:value-of select="$LINE_SEPARATOR"/>
		
	</xsl:template>

	<xsl:template match="ChangeOrders">
		<xsl:variable name="coverPage" select="current()/CoverPage"/>
		
		<xsl:apply-templates select="current()/AffectedItems">
			<xsl:with-param name="coverPage" select="$coverPage"/>
		</xsl:apply-templates>

	</xsl:template>

	<xsl:template match="AffectedItems">
		<xsl:param name="coverPage"/>
		<xsl:variable name="affectedItem" select="current()"/>
		<xsl:variable name="assembly" select="//Parts[@uniqueId=current()/@referentId] | //Documents[@uniqueId=current()/@referentId]"/>
		
		
		<xsl:value-of select="$affectedItem/ItemNumber"/>
		<xsl:value-of select="$LINE_SEPARATOR"/>
		
		<xsl:choose>
			<xsl:when test="$affectedItem/Redlines/RedlinedBOMRow">
				<xsl:for-each select="$affectedItem/Redlines/RedlinedBOMRow">
					<xsl:call-template name="RedlinedBOMRow">
						<xsl:with-param name="coverPage" select="$coverPage"/>
						<xsl:with-param name="affectedItem" select="$affectedItem"/>
						<xsl:with-param name="assembly" select="$assembly"/>
						<xsl:with-param name="redlineBomRow" select="current()"/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="RedlinedBOMRow">
					<xsl:with-param name="coverPage" select="$coverPage"/>
					<xsl:with-param name="affectedItem" select="$affectedItem"/>
					<xsl:with-param name="assembly" select="$assembly"/>
					<xsl:with-param name="redlineBomRow" select="$assembly"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose> 
		
	
		
	</xsl:template>

	<xsl:template name="RedlinedBOMRow">
		<xsl:param name="coverPage"/>
		<xsl:param name="affectedItem"/>
		<xsl:param name="assembly"/>
		<xsl:param name="redlineBomRow"/>
		
		<xsl:variable name="componentItemNumber">
			<xsl:choose>
				<xsl:when test="$redlineBomRow/RedlinedBOMRowCurrent/ItemNumber">
					<xsl:value-of select="$redlineBomRow/RedlinedBOMRowCurrent/ItemNumber"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$redlineBomRow/RedlinedBOMRowPrevious/ItemNumber"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="component" select="//Parts/TitleBlock[Number=$componentItemNumber]/.. | //Documents/TitleBlock[Number=$componentItemNumber]/.."/>
		
		<!-- Company -->
		<xsl:value-of select="$coverPage/ReasonCode"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- PartNum -->
		<xsl:value-of select="$affectedItem/ItemNumber"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- PartDescription -->
		<xsl:value-of select="$affectedItem/ItemDescription"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- TypeCode -->
		<xsl:value-of select="$assembly/PageTwo/List09"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- IUM -->
		<xsl:value-of select="$assembly/PageTwo/List01"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- LifeCycle -->
		<xsl:value-of select="$affectedItem/LifecyclePhase"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- TrackSerialNum -->
		<xsl:value-of select="$assembly/PageTwo/List02"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- SNFormat -->
		<xsl:value-of select="$assembly/PageTwo/List03"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- RestrictShipTo -->
		<xsl:value-of select="$assembly/PageTwo/List19"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- RestrictExcludeFrom -->
		<xsl:value-of select="$assembly/PageTwo/List20"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- PartRev -->
		<xsl:value-of select="$affectedItem/NewRev"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- RevDescription -->
		<xsl:value-of select="$coverPage/ChangeCategory"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- EffectiveDate -->
		<xsl:value-of select="$coverPage/DateOriginated"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- ECONum -->
		<xsl:value-of select="$coverPage/Number"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- ECODescription -->
		<xsl:value-of select="$coverPage/DescriptionOfChange"/>
		<xsl:value-of select="$coverPage/ReasonForChange"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMLevel -->
		<xsl:value-of select="'Unknown'"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMItemNum -->
		<xsl:value-of select="$componentItemNumber"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMDescription -->
		<xsl:value-of select="$component/TitleBlock/Description"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMRev -->
		<xsl:value-of select="$component/TitleBlock/Rev"/>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMQty -->
		<xsl:choose>
			<xsl:when test="$redlineBomRow/RedlinedBOMRowCurrent/Qty">
				<xsl:value-of select="$redlineBomRow/RedlinedBOMRowCurrent/Qty"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$redlineBomRow/RedlinedBOMRowPrevious/Qty"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMFindNum -->
		<xsl:choose>
			<xsl:when test="$redlineBomRow/RedlinedBOMRowCurrent/FindNum">
				<xsl:value-of select="$redlineBomRow/RedlinedBOMRowCurrent/FindNum"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$redlineBomRow/RedlinedBOMRowPrevious/FindNum"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="$VALUE_SEPARATOR"/>
		
		<!-- BOMUOM -->
		<xsl:choose>
			<xsl:when test="$redlineBomRow/RedlinedBOMRowCurrent/ItemList01">
				<xsl:value-of select="$redlineBomRow/RedlinedBOMRowCurrent/ItemList01"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$redlineBomRow/RedlinedBOMRowPrevious/ItemList01"/>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:value-of select="$LINE_SEPARATOR"/>
	</xsl:template>
</xsl:stylesheet>
