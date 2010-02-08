#line 1 "numpy/core/src/npymath/ieee754.c.src"

/*
 *****************************************************************************
 **       This file was autogenerated from a template  DO NOT EDIT!!!!      **
 **       Changes should be made to the original source (.src) file         **
 *****************************************************************************
 */

#line 1
/* -*- c -*- */
/*
 * vim:syntax=c
 *
 * Low-level routines related to IEEE-754 format
 */
#include "npy_math_common.h"
#include "npy_math_private.h"

#ifndef HAVE_COPYSIGN
double npy_copysign(double x, double y)
{
    npy_uint32 hx, hy;
    GET_HIGH_WORD(hx, x);
    GET_HIGH_WORD(hy, y);
    SET_HIGH_WORD(x, (hx & 0x7fffffff) | (hy & 0x80000000));
    return x;
}
#endif

#if !defined(HAVE_DECL_SIGNBIT)
#include "_signbit.c"

int _npy_signbit_f(float x)
{
    return _npy_signbit_d((double) x);
}

int _npy_signbit_ld(long double x)
{
    return _npy_signbit_d((double) x);
}
#endif

/*
 * FIXME: There is a lot of redundancy between _next* and npy_nextafter*.
 * refactor this at some point
 *
 * p >= 0, returnx x + nulp
 * p < 0, returnx x - nulp
 */
double _next(double x, int p)
{
    volatile double t;
    npy_int32 hx, hy, ix;
    npy_uint32 lx;

    EXTRACT_WORDS(hx, lx, x);
    ix = hx & 0x7fffffff;       /* |x| */

    if (((ix >= 0x7ff00000) && ((ix - 0x7ff00000) | lx) != 0))        /* x is nan */
        return x;
    if ((ix | lx) == 0) {       /* x == 0 */
        if (p >= 0) {
            INSERT_WORDS(x, 0x0, 1);    /* return +minsubnormal */
        } else {
            INSERT_WORDS(x, 0x80000000, 1);    /* return -minsubnormal */
        }
        t = x * x;
        if (t == x)
            return t;
        else
            return x;           /* raise underflow flag */
    }
    if (p < 0) {     /* x -= ulp */
        if (lx == 0)
            hx -= 1;
        lx -= 1;
    } else {         /* x += ulp */
        lx += 1;
        if (lx == 0)
            hx += 1;
    }
    hy = hx & 0x7ff00000;
    if (hy >= 0x7ff00000)
        return x + x;           /* overflow  */
    if (hy < 0x00100000) {      /* underflow */
        t = x * x;
        if (t != x) {           /* raise underflow flag */
            INSERT_WORDS(x, hx, lx);
            return x;
        }
    }
    INSERT_WORDS(x, hx, lx);
    return x;
}

float _nextf(float x, int p)
{
    volatile float t;
    npy_int32 hx, hy, ix;

    GET_FLOAT_WORD(hx, x);
    ix = hx & 0x7fffffff;       /* |x| */

    if ((ix > 0x7f800000))      /* x is nan */
        return x;
    if (ix == 0) {              /* x == 0 */
        if (p >= 0) {
            SET_FLOAT_WORD(x, 0x0 | 1); /* return +minsubnormal */
        } else {
            SET_FLOAT_WORD(x, 0x80000000 | 1); /* return -minsubnormal */
        }
        t = x * x;
        if (t == x)
            return t;
        else
            return x;           /* raise underflow flag */
    }
    if (p < 0) {            /* x -= ulp */
        hx -= 1;
    } else {                /* x += ulp */
        hx += 1;
    }
    hy = hx & 0x7f800000;
    if (hy >= 0x7f800000)
        return x + x;           /* overflow  */
    if (hy < 0x00800000) {      /* underflow */
        t = x * x;
        if (t != x) {           /* raise underflow flag */
            SET_FLOAT_WORD(x, hx);
            return x;
        }
    }
    SET_FLOAT_WORD(x, hx);
    return x;
}

npy_longdouble _nextl(npy_longdouble x, int p)
{
    volatile npy_longdouble t;
    union IEEEl2bitsrep ux;

    ux.e = x;

    if ((GET_LDOUBLE_EXP(ux) == 0x7fff &&
         ((GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT) | GET_LDOUBLE_MANL(ux)) != 0)) {
        return ux.e;        /* x is nan */
    }
    if (ux.e == 0.0) {
        SET_LDOUBLE_MANH(ux, 0);              /* return +-minsubnormal */
        SET_LDOUBLE_MANL(ux, 1);
        if (p >= 0) {
            SET_LDOUBLE_SIGN(ux, 0);
        } else {
            SET_LDOUBLE_SIGN(ux, 1);
        }
        t = ux.e * ux.e;
        if (t == ux.e) {
            return t;
        } else {
            return ux.e;           /* raise underflow flag */
        }
    }
    if (p < 0) {      /* x -= ulp */
        if (GET_LDOUBLE_MANL(ux) == 0) {
            if ((GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT) == 0) {
                SET_LDOUBLE_EXP(ux, GET_LDOUBLE_EXP(ux) - 1);
            }
            SET_LDOUBLE_MANH(ux,
                             (GET_LDOUBLE_MANH(ux) - 1) |
                             (GET_LDOUBLE_MANH(ux) & LDBL_NBIT));
        }
        SET_LDOUBLE_MANL(ux, GET_LDOUBLE_MANL(ux) - 1);
    } else {                    /* x += ulp */
        SET_LDOUBLE_MANL(ux, GET_LDOUBLE_MANL(ux) + 1);
        if (GET_LDOUBLE_MANL(ux) == 0) {
            SET_LDOUBLE_MANH(ux,
                             (GET_LDOUBLE_MANH(ux) + 1) |
                             (GET_LDOUBLE_MANH(ux) & LDBL_NBIT));
            if ((GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT) == 0) {
                SET_LDOUBLE_EXP(ux, GET_LDOUBLE_EXP(ux) + 1);
            }
        }
    }
    if (GET_LDOUBLE_EXP(ux) == 0x7fff) {
        return ux.e + ux.e;           /* overflow  */
    }
    if (GET_LDOUBLE_EXP(ux) == 0) {            /* underflow */
        if (LDBL_NBIT) {
            SET_LDOUBLE_MANH(ux, GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT);
        }
        t = ux.e * ux.e;
        if (t != ux.e) {           /* raise underflow flag */
            return ux.e;
        }
    }

    return ux.e;
}

/*
 * nextafter code taken from BSD math lib, the code contains the following
 * notice:
 *
 * ====================================================
 * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
 *
 * Developed at SunPro, a Sun Microsystems, Inc. business.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice
 * is preserved.
 * ====================================================
 */

#ifndef HAVE_NEXTAFTER
double npy_nextafter(double x, double y)
{
    volatile double t;
    npy_int32 hx, hy, ix, iy;
    npy_uint32 lx, ly;

    EXTRACT_WORDS(hx, lx, x);
    EXTRACT_WORDS(hy, ly, y);
    ix = hx & 0x7fffffff;       /* |x| */
    iy = hy & 0x7fffffff;       /* |y| */

    if (((ix >= 0x7ff00000) && ((ix - 0x7ff00000) | lx) != 0) ||        /* x is nan */
        ((iy >= 0x7ff00000) && ((iy - 0x7ff00000) | ly) != 0))  /* y is nan */
        return x + y;
    if (x == y)
        return y;               /* x=y, return y */
    if ((ix | lx) == 0) {       /* x == 0 */
        INSERT_WORDS(x, hy & 0x80000000, 1);    /* return +-minsubnormal */
        t = x * x;
        if (t == x)
            return t;
        else
            return x;           /* raise underflow flag */
    }
    if (hx >= 0) {              /* x > 0 */
        if (hx > hy || ((hx == hy) && (lx > ly))) {     /* x > y, x -= ulp */
            if (lx == 0)
                hx -= 1;
            lx -= 1;
        } else {                /* x < y, x += ulp */
            lx += 1;
            if (lx == 0)
                hx += 1;
        }
    } else {                    /* x < 0 */
        if (hy >= 0 || hx > hy || ((hx == hy) && (lx > ly))) {  /* x < y, x -= ulp */
            if (lx == 0)
                hx -= 1;
            lx -= 1;
        } else {                /* x > y, x += ulp */
            lx += 1;
            if (lx == 0)
                hx += 1;
        }
    }
    hy = hx & 0x7ff00000;
    if (hy >= 0x7ff00000)
        return x + x;           /* overflow  */
    if (hy < 0x00100000) {      /* underflow */
        t = x * x;
        if (t != x) {           /* raise underflow flag */
            INSERT_WORDS(y, hx, lx);
            return y;
        }
    }
    INSERT_WORDS(x, hx, lx);
    return x;
}
#endif

#ifndef HAVE_NEXTAFTERF
float npy_nextafterf(float x, float y)
{
    volatile float t;
    npy_int32 hx, hy, ix, iy;

    GET_FLOAT_WORD(hx, x);
    GET_FLOAT_WORD(hy, y);
    ix = hx & 0x7fffffff;       /* |x| */
    iy = hy & 0x7fffffff;       /* |y| */

    if ((ix > 0x7f800000) ||    /* x is nan */
        (iy > 0x7f800000))      /* y is nan */
        return x + y;
    if (x == y)
        return y;               /* x=y, return y */
    if (ix == 0) {              /* x == 0 */
        SET_FLOAT_WORD(x, (hy & 0x80000000) | 1); /* return +-minsubnormal */
        t = x * x;
        if (t == x)
            return t;
        else
            return x;           /* raise underflow flag */
    }
    if (hx >= 0) {              /* x > 0 */
        if (hx > hy) {          /* x > y, x -= ulp */
            hx -= 1;
        } else {                /* x < y, x += ulp */
            hx += 1;
        }
    } else {                    /* x < 0 */
        if (hy >= 0 || hx > hy) {       /* x < y, x -= ulp */
            hx -= 1;
        } else {                /* x > y, x += ulp */
            hx += 1;
        }
    }
    hy = hx & 0x7f800000;
    if (hy >= 0x7f800000)
        return x + x;           /* overflow  */
    if (hy < 0x00800000) {      /* underflow */
        t = x * x;
        if (t != x) {           /* raise underflow flag */
            SET_FLOAT_WORD(y, hx);
            return y;
        }
    }
    SET_FLOAT_WORD(x, hx);
    return x;
}
#endif

#ifndef HAVE_NEXTAFTERL
npy_longdouble npy_nextafterl(npy_longdouble x, npy_longdouble y)
{
    volatile npy_longdouble t;
    union IEEEl2bitsrep ux;
    union IEEEl2bitsrep uy;

    ux.e = x;
    uy.e = y;

    if ((GET_LDOUBLE_EXP(ux) == 0x7fff &&
         ((GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT) | GET_LDOUBLE_MANL(ux)) != 0) ||
        (GET_LDOUBLE_EXP(uy) == 0x7fff &&
         ((GET_LDOUBLE_MANH(uy) & ~LDBL_NBIT) | GET_LDOUBLE_MANL(uy)) != 0)) {
        return ux.e + uy.e;        /* x or y is nan */
    }
    if (ux.e == uy.e) {
        return uy.e;               /* x=y, return y */
    }
    if (ux.e == 0.0) {
        SET_LDOUBLE_MANH(ux, 0);              /* return +-minsubnormal */
        SET_LDOUBLE_MANL(ux, 1);
        SET_LDOUBLE_SIGN(ux, GET_LDOUBLE_SIGN(uy));
        t = ux.e * ux.e;
        if (t == ux.e) {
            return t;
        } else {
            return ux.e;           /* raise underflow flag */
        }
    }
    if ((ux.e > 0.0) ^ (ux.e < uy.e)) {      /* x -= ulp */
        if (GET_LDOUBLE_MANL(ux) == 0) {
            if ((GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT) == 0) {
                SET_LDOUBLE_EXP(ux, GET_LDOUBLE_EXP(ux) - 1);
            }
            SET_LDOUBLE_MANH(ux,
                             (GET_LDOUBLE_MANH(ux) - 1) |
                             (GET_LDOUBLE_MANH(ux) & LDBL_NBIT));
        }
        SET_LDOUBLE_MANL(ux, GET_LDOUBLE_MANL(ux) - 1);
    } else {                    /* x += ulp */
        SET_LDOUBLE_MANL(ux, GET_LDOUBLE_MANL(ux) + 1);
        if (GET_LDOUBLE_MANL(ux) == 0) {
            SET_LDOUBLE_MANH(ux,
                             (GET_LDOUBLE_MANH(ux) + 1) |
                             (GET_LDOUBLE_MANH(ux) & LDBL_NBIT));
            if ((GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT) == 0) {
                SET_LDOUBLE_EXP(ux, GET_LDOUBLE_EXP(ux) + 1);
            }
        }
    }
    if (GET_LDOUBLE_EXP(ux) == 0x7fff) {
        return ux.e + ux.e;           /* overflow  */
    }
    if (GET_LDOUBLE_EXP(ux) == 0) {            /* underflow */
        if (LDBL_NBIT) {
            SET_LDOUBLE_MANH(ux, GET_LDOUBLE_MANH(ux) & ~LDBL_NBIT);
        }
        t = ux.e * ux.e;
        if (t != ux.e) {           /* raise underflow flag */
            return ux.e;
        }
    }

    return ux.e;
}
#endif

#line 392
float npy_spacingf(float x)
{
    /* XXX: npy isnan/isinf may be optimized by bit twiddling */
    if (npy_isinf(x)) {
        return NPY_NANF;
    }

    return _nextf(x, 1) - x;
}

#line 392
double npy_spacing(double x)
{
    /* XXX: npy isnan/isinf may be optimized by bit twiddling */
    if (npy_isinf(x)) {
        return NPY_NAN;
    }

    return _next(x, 1) - x;
}

#line 392
npy_longdouble npy_spacingl(npy_longdouble x)
{
    /* XXX: npy isnan/isinf may be optimized by bit twiddling */
    if (npy_isinf(x)) {
        return NPY_NANL;
    }

    return _nextl(x, 1) - x;
}


/*
 * Decorate all the math functions which are available on the current platform
 */

#ifdef HAVE_NEXTAFTERF
float npy_nextafterf(float x, float y)
{
    return nextafterf(x, y);
}
#endif

#ifdef HAVE_NEXTAFTER
double npy_nextafter(double x, double y)
{
    return nextafter(x, y);
}
#endif

#ifdef HAVE_NEXTAFTERL
npy_longdouble npy_nextafterl(npy_longdouble x, npy_longdouble y)
{
    return nextafterl(x, y);
}
#endif

