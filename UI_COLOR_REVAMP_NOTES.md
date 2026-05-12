# UI Color Revamp Notes

Applied the requested cool neutral color system across the app:

- Page background: `#F7F7F5`
- Search/input soft background: `#F1F1ED`
- Card borders: `#E8E8E3`
- Inner dividers: `#EDEDE8`

Also kept the working Network search behavior and removed the non-functional Network filter chips.

Backend persistence note: `spring.jpa.hibernate.ddl-auto` is set to `update` so local MySQL users/posts are not dropped on every backend restart.
