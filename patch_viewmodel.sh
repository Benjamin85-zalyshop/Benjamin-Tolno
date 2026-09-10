sed -i 's/maxScore = doc.getDouble("maxScore")?.toFloat() ?: 20f/maxScore = (doc.get("maxScore") as? Number)?.toFloat() ?: if ((doc.getString("section") ?: "") in listOf("LE PRIMAIRE", "LA MATERNELLE")) 10f else 20f/g' app/src/main/java/com/example/ui/SchoolViewModel.kt

sed -i 's/maxScore = subjectDoc.getDouble("maxScore")?.toFloat() ?: 20f/maxScore = (subjectDoc.get("maxScore") as? Number)?.toFloat() ?: if ((subjectDoc.getString("section") ?: "") in listOf("LE PRIMAIRE", "LA MATERNELLE")) 10f else 20f/g' app/src/main/java/com/example/ui/SchoolViewModel.kt
