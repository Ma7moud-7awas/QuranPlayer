package com.m7.quranplayer.chapter.domain.model

/** All Quran parts that each [Chapter] belongs to. */
enum class Part(val chapters: IntRange) {
    First(1..2),
    Second(2..2),
    Third(2..3),
    Fourth(3..4),
    Fifth(4..4),
    Sixth(4..5),
    Seventh(5..6),
    Eighth(6..7),
    Ninth(7..8),
    Tenth(8..9),
    Eleventh(9..11),
    Twelfth(11..12),
    Thirteenth(12..14),
    Fourteenth(15..16),
    Fifteenth(17..18),
    Sixteenth(18..20),
    Seventeenth(21..22),
    Eighteenth(23..25),
    Nineteenth(25..27),
    Twentieth(27..29),
    TwentyFirst(29..33),
    TwentySecond(33..36),
    TwentyThird(36..39),
    TwentyFourth(39..41),
    TwentyFifth(41..45),
    TwentySixth(46..51),
    TwentySeventh(51..57),
    TwentyEighth(58..66),
    TwentyNinth(67..77),
    Thirtieth(78..114);

    companion object {
        fun getPartsByChapterNumber(number: Int): List<Part> {
            return entries.filter { number in it.chapters }
        }
    }
}
