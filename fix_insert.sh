sed -i 's/maxScore = maxScore,/maxScore = if (section == "LE PRIMAIRE" || section == "LA MATERNELLE") 10f else maxScore,/g' app/src/main/java/com/example/ui/SchoolViewModel.kt
