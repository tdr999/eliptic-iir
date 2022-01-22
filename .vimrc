set shortmess=a
"set cmdheight=2"
"set nocompatible"
set background=dark
hi clear
syntax on
hi Normal		ctermbg=Black	ctermfg=DarkGray	guibg=Black	guifg=DarkGray
hi Comment		term=italic	ctermbg=Black		ctermfg=DarkYellow	guifg=DarkYellow	guibg=Black
hi Constant		term=underline	ctermfg=DarkYellow 	guifg=DarkYellow		
hi Identifier		term=underline	ctermfg=DarkRed  	guifg=DarkRed
hi Ignore		ctermfg=black	guifg=bg
hi PreProc		term=italic	ctermfg=DarkGreen  	guifg=DarkGreen
hi Search		term=bold	ctermbg=Yellow		guifg=white	guibg=Black
hi Special		term=bold	ctermfg=LightRed   	guifg=LightRed
hi Statement		term=italic	ctermfg=Red	  	guifg=Red	
hi Type					ctermfg=Red		guifg=Red	
hi Error		term=reverse   	ctermbg=Red	  	ctermfg=White 	guibg=Red 	guifg=White
hi Todo			term=italic  	ctermbg=Red		ctermfg=Black 	guifg=Blue 	guibg=DarkRed
" From the source:
hi Cursor		ctermbg=Brown	ctermfg=Brown   	guifg=Brown		guibg=fg
hi Directory		term=bold	ctermfg=LightCyan  	guifg=LightCyan
hi ErrorMsg		term=standout	ctermbg=DarkRed	  	ctermfg=DarkRed 	guibg=Red	guifg=White
hi IncSearch		term=reverse	ctermbg=Yellow	  	gui=reverse
hi LineNr		term=underline	ctermfg=DarkYellow	guifg=DarkYellow
hi ModeMsg		term=bold	cterm=bold		gui=bold
hi MoreMsg		term=bold	ctermfg=LightGreen 	gui=bold	guifg=SeaGreen
hi NonText		term=bold	ctermfg=DarkYellow	gui=bold	guifg=Blue
hi Question		term=standout  	ctermfg=LightGreen 	gui=bold	guifg=Cyan
hi SpecialKey		term=bold	ctermfg=DarkRed  	guifg=DarkRed
hi StatusLine		term=bold 	cterm=reverse   	gui=NONE	guifg=Red guibg=Black
hi StatusLineNC 	term=reverse   	cterm=reverse	  	gui=NONE	guifg=white guibg=#333333
hi Title		term=bold	ctermfg=LightMagenta 	gui=bold	guifg=LightMagenta
hi WarningMsg		term=standout  	ctermfg=LightRed   	guifg=Red

set nocompatible
set number
set cindent
set autoindent
set wildmenu
"set cursorcolumn"
set ruler
set ts=4 sts=4 sw=4 et
set laststatus=2
set statusline=
set statusline+=%#function#\ %l
set statusline+=\ %*
set statusline+=\ (
set statusline+=\ %f\ %*
set statusline+=\ )
set statusline+=\ %m
set statusline+=%#keyword#\ %F
set statusline+=%=
"set statusline+=\ %{LinterStatus()}"
set statusline+=\ (
set statusline+=\ %{strftime('%R',getftime(expand('%')))}
set statusline+=\ ::
set statusline+=\ %n
set statusline+=\ )\ %*
"https://kadekillary.work/post/statusline-vim/"

set omnifunc=syntaxcomplete#Complete
set complete+=kspell
set completeopt=menuone,longest
