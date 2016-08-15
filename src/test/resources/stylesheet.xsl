<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <body>
                <h2 align="center">The result of filtering rules:</h2>
                <table border="1" align="center">
                    <tr bgcolor="#c0c0c0">
                        <th>Name </th>
                        <th>Type</th>
                        <th>Weight </th>
                    </tr>
                    <xsl:apply-templates select="rules"/>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="rules">
        <xsl:for-each select="rule">
            <tr>
                <td align="center"><xsl:value-of select="@name" /></td>
                <td align="center"><xsl:value-of select="@type" /></td>
                <td align="center"><xsl:value-of select="@weight" /></td>
            </tr>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>