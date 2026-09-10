cat << 'INNER_EOF' > temp1160.kt
                    val parsedSection = doc.getString("section") ?: ""
                    val parsedMaxScore = if (parsedSection == "LE PRIMAIRE" || parsedSection == "LA MATERNELLE") 10f else ((doc.get("maxScore") as? Number)?.toFloat() ?: 20f)
INNER_EOF
sed -i -e '1156,1157c\' -e "$(cat temp1160.kt)" app/src/main/java/com/example/ui/SchoolViewModel.kt
