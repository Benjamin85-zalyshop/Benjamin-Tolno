                                val parsedSection = subjectDoc.getString("section") ?: ""
                                val parsedMaxScore = if (parsedSection == "LE PRIMAIRE" || parsedSection == "LA MATERNELLE") 10f else ((subjectDoc.get("maxScore") as? Number)?.toFloat() ?: 20f)
                                val subject = Subject(
                                    id = 0, schoolId = schoolId,
                                    section = parsedSection, grade = subjectDoc.getString("grade") ?: "",
                                    name = subjectDoc.getString("name") ?: "", coefficient = subjectDoc.getLong("coefficient")?.toInt() ?: 1,
                                    maxScore = parsedMaxScore, remoteId = subjectRemoteId
                                )
