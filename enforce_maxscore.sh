sed -i 's/val subject = Subject(/val parsedSection = doc.getString("section") ?: ""\n                    val parsedMaxScore = if (parsedSection == "LE PRIMAIRE" || parsedSection == "LA MATERNELLE") 10f else ((doc.get("maxScore") as? Number)?.toFloat() ?: 20f)\n\n                    val subject = Subject(/g' app/src/main/java/com/example/ui/SchoolViewModel.kt

sed -i 's/section = doc.getString("section") ?: "",/section = parsedSection,/g' app/src/main/java/com/example/ui/SchoolViewModel.kt
