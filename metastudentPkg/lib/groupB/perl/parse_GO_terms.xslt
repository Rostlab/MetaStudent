<xsl:stylesheet version='1.0'  xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:output method="text" omit-xml-declaration="yes"/>
<xsl:template match="/">#term	name
<xsl:for-each select="obo/term">
	<xsl:value-of select="id"/>
	<xsl:text>	</xsl:text>
	<xsl:value-of select="name"/>
	<xsl:text>
</xsl:text>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>
